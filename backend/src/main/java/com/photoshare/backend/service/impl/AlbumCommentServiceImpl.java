package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.AlbumCommentDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.mapper.AlbumCommentMapper;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.UserMapper;
import com.photoshare.backend.service.AlbumCommentService;
import com.photoshare.backend.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AlbumCommentServiceImpl implements AlbumCommentService {

    private static final int MAX_MENTIONS = 20;

    @Autowired
    private AlbumCommentMapper albumCommentMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<AlbumComment> listComments(String albumId) {
        return albumCommentMapper.selectByAlbumId(albumId);
    }

    @Override
    public AlbumComment addComment(String albumId, AlbumCommentDTO dto, String userId) {
        if (albumMapper.selectById(albumId) == null) {
            throw new RuntimeException("相册不存在");
        }
        AlbumComment comment = new AlbumComment();
        comment.setId(UUID.randomUUID().toString());
        comment.setAlbumId(albumId);
        comment.setUserId(userId);
        comment.setContent(dto.getContent().trim());
        comment.setReplyToUserId(resolveParent(comment, dto.getParentId()));
        albumCommentMapper.insert(comment);

        AlbumComment saved = albumCommentMapper.selectById(comment.getId());
        notificationService.onComment(saved, resolveMentions(dto.getMentionUserIds(), userId));
        return saved;
    }

    @Override
    public void deleteComment(String commentId, String userId) {
        AlbumComment comment = albumCommentMapper.selectById(commentId);
        if (comment == null || Boolean.TRUE.equals(comment.getIsDeleted())) {
            throw new RuntimeException("评论不存在");
        }
        // 评论作者可以删自己的；相册所有者可以删相册下的任意评论
        if (!comment.getUserId().equals(userId)) {
            Album album = albumMapper.selectById(comment.getAlbumId());
            if (album == null || !album.getUserId().equals(userId)) {
                throw new RuntimeException("无权删除这条评论");
            }
        }
        albumCommentMapper.softDeleteById(commentId);
        if (comment.getParentId() == null) {
            albumCommentMapper.softDeleteByParentId(commentId);
        }
    }

    /**
     * 回复只做一级：回复一条回复时挂到同一条一级评论下，返回值是被应答的人，用于显示"回复 @某人"。
     */
    private String resolveParent(AlbumComment comment, String parentId) {
        if (parentId == null || parentId.trim().isEmpty()) {
            return null;
        }
        AlbumComment parent = albumCommentMapper.selectById(parentId.trim());
        if (parent == null || Boolean.TRUE.equals(parent.getIsDeleted())
                || !comment.getAlbumId().equals(parent.getAlbumId())) {
            throw new RuntimeException("要回复的评论不存在");
        }
        comment.setParentId(parent.getParentId() == null ? parent.getId() : parent.getParentId());
        return parent.getUserId();
    }

    // 只保留真实存在的用户，避免前端塞入任意ID刷通知
    private List<String> resolveMentions(List<String> mentionUserIds, String authorId) {
        List<String> valid = new ArrayList<>();
        if (mentionUserIds == null) {
            return valid;
        }
        Set<String> seen = new HashSet<>();
        for (String id : mentionUserIds) {
            if (id == null || id.isEmpty() || id.equals(authorId) || !seen.add(id) || valid.size() >= MAX_MENTIONS) {
                continue;
            }
            if (userMapper.selectBriefById(id) != null) {
                valid.add(id);
            }
        }
        return valid;
    }
}
