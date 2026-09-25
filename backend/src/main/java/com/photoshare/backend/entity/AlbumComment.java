package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AlbumComment {

    private String id;

    private String albumId;

    // 一级回复：指向所属的一级评论，NULL 表示本身就是一级评论
    private String parentId;

    // 被回复的人，用于显示"回复 @某人"
    private String replyToUserId;

    private String userId;

    private String content;

    private Boolean isDeleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 以下字段由查询关联得到，不写入评论表
    private String albumTitle;

    private String userName;

    private String avatarUrl;

    private String replyToUserName;
}
