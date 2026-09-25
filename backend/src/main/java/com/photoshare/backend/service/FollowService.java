package com.photoshare.backend.service;

import com.photoshare.backend.dto.UserBriefDTO;
import com.photoshare.backend.dto.UserProfileDTO;

import java.util.List;

public interface FollowService {

    void follow(String followeeId, String followerId);

    void unfollow(String followeeId, String followerId);

    UserProfileDTO getProfile(String userId, String viewerId);

    List<UserBriefDTO> listFollowers(String userId, String viewerId, int page, int size);

    List<UserBriefDTO> listFollowing(String userId, String viewerId, int page, int size);

    /** @ 提及候选与关注搜索，keyword 留空时给出默认列表 */
    List<UserBriefDTO> suggestUsers(String keyword, String viewerId);
}
