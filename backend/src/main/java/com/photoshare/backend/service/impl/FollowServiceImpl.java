package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.dto.UserBriefDTO;
import com.photoshare.backend.dto.UserProfileDTO;
import com.photoshare.backend.entity.User;
import com.photoshare.backend.entity.UserFollow;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.UserFollowMapper;
import com.photoshare.backend.mapper.UserMapper;
import com.photoshare.backend.service.FollowService;
import com.photoshare.backend.service.NotificationService;
import com.photoshare.backend.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FollowServiceImpl implements FollowService {

    private static final int PROFILE_ALBUMS = 48;

    private static final int SUGGEST_LIMIT = 10;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private SearchService searchService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void follow(String followeeId, String followerId) {
        if (followeeId == null || followeeId.equals(followerId)) {
            throw new RuntimeException("不能关注自己");
        }
        if (userMapper.selectBriefById(followeeId) == null) {
            throw new RuntimeException("用户不存在");
        }
        // 重复关注直接忽略，保证前端连点不会报错
        if (userFollowMapper.exists(followerId, followeeId) > 0) {
            return;
        }
        UserFollow userFollow = new UserFollow();
        userFollow.setId(UUID.randomUUID().toString());
        userFollow.setFollowerId(followerId);
        userFollow.setFolloweeId(followeeId);
        userFollowMapper.insert(userFollow);
        notificationService.onFollowed(followerId, followeeId);
    }

    @Override
    public void unfollow(String followeeId, String followerId) {
        if (userFollowMapper.delete(followerId, followeeId) > 0) {
            notificationService.onUnfollowed(followerId, followeeId);
        }
    }

    @Override
    public UserProfileDTO getProfile(String userId, String viewerId) {
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new RuntimeException("用户不存在");
        }
        boolean self = userId.equals(viewerId);
        boolean followedByMe = !self && userFollowMapper.exists(viewerId, userId) > 0;

        ContentSearchDTO query = new ContentSearchDTO();
        query.setOwnerId(userId);
        query.setSize(PROFILE_ALBUMS);

        UserProfileDTO profile = new UserProfileDTO();
        profile.setId(target.getId());
        profile.setUsername(target.getUsername());
        profile.setNickname(target.getNickname() == null ? target.getUsername() : target.getNickname());
        profile.setAvatarUrl(target.getAvatarUrl());
        profile.setCreatedAt(target.getCreatedAt());
        profile.setSelf(self);
        profile.setFollowedByMe(followedByMe);
        profile.setCanViewContent(self || followedByMe);
        profile.setFollowerCount(userFollowMapper.countFollowers(userId));
        profile.setFollowingCount(userFollowMapper.countFollowing(userId));
        profile.setAlbumCount(self
                ? albumMapper.countByUserId(userId)
                : albumMapper.countPublicByUserId(userId));
        profile.setAlbums(searchService.searchAlbums(query, viewerId));
        return profile;
    }

    @Override
    public List<UserBriefDTO> listFollowers(String userId, String viewerId, int page, int size) {
        return userFollowMapper.selectFollowers(userId, viewerId, pageSize(size), offset(page, size));
    }

    @Override
    public List<UserBriefDTO> listFollowing(String userId, String viewerId, int page, int size) {
        return userFollowMapper.selectFollowing(userId, viewerId, pageSize(size), offset(page, size));
    }

    @Override
    public List<UserBriefDTO> suggestUsers(String keyword, String viewerId) {
        return userMapper.selectSuggested(keyword == null ? "" : keyword.trim(), viewerId, SUGGEST_LIMIT);
    }

    private int pageSize(int size) {
        return size < 1 || size > 50 ? 20 : size;
    }

    private int offset(int page, int size) {
        return (page < 1 ? 0 : page - 1) * pageSize(size);
    }
}
