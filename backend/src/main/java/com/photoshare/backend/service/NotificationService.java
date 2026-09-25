package com.photoshare.backend.service;

import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.entity.Notification;

import java.util.List;

public interface NotificationService {

    void onLiked(String targetType, String targetId, String actorId);

    void onLikeRemoved(String targetType, String targetId, String actorId);

    /** 评论落库后调用：通知相册作者或被回复人，再给 @ 到的人各发一条提及通知 */
    void onComment(AlbumComment comment, List<String> mentionUserIds);

    void onFollowed(String followerId, String followeeId);

    void onUnfollowed(String followerId, String followeeId);

    List<Notification> list(String userId, boolean unreadOnly, int page, int size);

    int unreadCount(String userId);

    void markRead(String id, String userId);

    void markAllRead(String userId);

    void remove(String id, String userId);
}
