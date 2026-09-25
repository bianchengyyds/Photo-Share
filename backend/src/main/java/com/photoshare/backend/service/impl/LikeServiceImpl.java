package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.LikeSummaryDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.UserLike;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.mapper.UserLikeMapper;
import com.photoshare.backend.service.LikeService;
import com.photoshare.backend.service.NotificationService;
import com.photoshare.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LikeServiceImpl implements LikeService {

    private static final String TARGET_ALBUM = "ALBUM";
    private static final String TARGET_PHOTO = "PHOTO";

    @Autowired
    private UserLikeMapper userLikeMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private TagService tagService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void like(String targetType, String targetId, String userId) {
        String type = normalizeType(targetType);
        assertTargetExists(type, targetId);

        // 重复点赞直接忽略，保证前端连点不会报错
        if (userLikeMapper.exists(userId, type, targetId) > 0) {
            return;
        }
        UserLike userLike = new UserLike();
        userLike.setId(UUID.randomUUID().toString());
        userLike.setUserId(userId);
        userLike.setTargetType(type);
        userLike.setTargetId(targetId);
        userLikeMapper.insert(userLike);
        notificationService.onLiked(type, targetId, userId);
    }

    @Override
    public void unlike(String targetType, String targetId, String userId) {
        String type = normalizeType(targetType);
        if (userLikeMapper.delete(userId, type, targetId) > 0) {
            notificationService.onLikeRemoved(type, targetId, userId);
        }
    }

    @Override
    public LikeSummaryDTO getMyLikes(String userId) {
        LikeSummaryDTO summary = new LikeSummaryDTO();
        summary.setLikedAlbumIds(userLikeMapper.selectLikedAlbumIds(userId));
        summary.setLikedPhotoIds(userLikeMapper.selectLikedPhotoIds(userId));
        return summary;
    }

    @Override
    public List<Album> listLikedAlbums(String userId) {
        List<Album> albums = userLikeMapper.selectLikedAlbums(userId);
        for (Album album : albums) {
            album.setTags(tagService.listTags(TARGET_ALBUM, album.getId()));
        }
        return albums;
    }

    @Override
    public List<Photo> listLikedPhotos(String userId) {
        List<Photo> photos = userLikeMapper.selectLikedPhotos(userId);
        tagService.attachPhotoTags(photos);
        return photos;
    }

    private String normalizeType(String targetType) {
        String type = targetType == null ? "" : targetType.trim().toUpperCase();
        if (!TARGET_ALBUM.equals(type) && !TARGET_PHOTO.equals(type)) {
            throw new RuntimeException("点赞目标类型只能是 ALBUM 或 PHOTO");
        }
        return type;
    }

    private void assertTargetExists(String type, String targetId) {
        if (TARGET_ALBUM.equals(type)) {
            if (albumMapper.selectById(targetId) == null) {
                throw new RuntimeException("相册不存在");
            }
        } else if (photoMapper.selectById(targetId) == null) {
            throw new RuntimeException("照片不存在");
        }
    }
}
