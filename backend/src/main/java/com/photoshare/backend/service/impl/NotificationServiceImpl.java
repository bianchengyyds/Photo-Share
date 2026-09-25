package com.photoshare.backend.service.impl;

import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.entity.Notification;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.NotificationMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final int SNIPPET_LENGTH = 200;

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Override
    public void onLiked(String targetType, String targetId, String actorId) {
        String recipient = resolveOwner(targetType, targetId);
        if (recipient == null || recipient.equals(actorId)) {
            return;
        }
        create(recipient, actorId, Notification.TYPE_LIKE, targetType, targetId, null);
    }

    @Override
    public void onLikeRemoved(String targetType, String targetId, String actorId) {
        notificationMapper.deleteByEvent(actorId, Notification.TYPE_LIKE, targetType, targetId);
    }

    @Override
    public void onComment(AlbumComment comment, List<String> mentionUserIds) {
        boolean isReply = comment.getParentId() != null;
        // 一级评论通知相册作者，回复通知被回复的人
        String recipient = isReply ? comment.getReplyToUserId() : albumOwnerId(comment.getAlbumId());
        String type = isReply ? Notification.TYPE_REPLY : Notification.TYPE_COMMENT;
        String snippet = snippet(comment.getContent());
        if (recipient != null && !recipient.equals(comment.getUserId())) {
            create(recipient, comment.getUserId(), type, Notification.TARGET_ALBUM, comment.getAlbumId(), snippet);
        }
        for (String mentioned : normalizeMentions(mentionUserIds)) {
            if (mentioned.equals(comment.getUserId()) || mentioned.equals(recipient)) {
                continue;
            }
            create(mentioned, comment.getUserId(), Notification.TYPE_MENTION,
                    Notification.TARGET_ALBUM, comment.getAlbumId(), snippet);
        }
    }

    @Override
    public void onFollowed(String followerId, String followeeId) {
        create(followeeId, followerId, Notification.TYPE_FOLLOW, Notification.TARGET_USER, followerId, null);
    }

    @Override
    public void onUnfollowed(String followerId, String followeeId) {
        notificationMapper.deleteByEvent(followerId, Notification.TYPE_FOLLOW,
                Notification.TARGET_USER, followerId);
    }

    @Override
    public List<Notification> list(String userId, boolean unreadOnly, int page, int size) {
        int limit = size < 1 || size > MAX_PAGE_SIZE ? 20 : size;
        int offset = (page < 1 ? 0 : page - 1) * limit;
        return unreadOnly
                ? notificationMapper.selectUnreadByUser(userId, limit, offset)
                : notificationMapper.selectByUser(userId, limit, offset);
    }

    @Override
    public int unreadCount(String userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markRead(String id, String userId) {
        notificationMapper.markRead(userId, id);
    }

    @Override
    public void markAllRead(String userId) {
        notificationMapper.markAllRead(userId);
    }

    @Override
    public void remove(String id, String userId) {
        notificationMapper.deleteByIdAndUser(userId, id);
    }

    private List<String> normalizeMentions(List<String> mentionUserIds) {
        List<String> result = new ArrayList<>();
        if (mentionUserIds == null) {
            return result;
        }
        Set<String> seen = new HashSet<>();
        for (String id : mentionUserIds) {
            if (id != null && !id.isEmpty() && seen.add(id)) {
                result.add(id);
            }
        }
        return result;
    }

    private String albumOwnerId(String albumId) {
        Album album = albumMapper.selectById(albumId);
        return album == null ? null : album.getUserId();
    }

    private String resolveOwner(String targetType, String targetId) {
        if (Notification.TARGET_ALBUM.equals(targetType)) {
            return albumOwnerId(targetId);
        }
        if (Notification.TARGET_USER.equals(targetType)) {
            return targetId;
        }
        Photo photo = photoMapper.selectById(targetId);
        // 照片被点赞时通知上传者，而不是相册主人
        return photo == null ? null : photo.getUserId();
    }

    private String snippet(String content) {
        if (content == null) {
            return null;
        }
        return content.length() <= SNIPPET_LENGTH
                ? content
                : content.substring(0, SNIPPET_LENGTH - 3) + "...";
    }

    private void create(String userId, String actorId, String type, String targetType,
                        String targetId, String snippet) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setUserId(userId);
        notification.setActorId(actorId);
        notification.setType(type);
        notification.setTargetType(targetType);
        notification.setTargetId(targetId);
        notification.setSnippet(snippet);
        notificationMapper.insert(notification);
    }
}
