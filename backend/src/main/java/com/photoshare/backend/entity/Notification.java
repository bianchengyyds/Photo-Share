package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Notification {

    public static final String TYPE_LIKE = "LIKE";

    public static final String TYPE_COMMENT = "COMMENT";

    public static final String TYPE_REPLY = "REPLY";

    public static final String TYPE_FOLLOW = "FOLLOW";

    public static final String TYPE_MENTION = "MENTION";

    public static final String TARGET_ALBUM = "ALBUM";

    public static final String TARGET_USER = "USER";

    private String id;

    private String userId;

    private String actorId;

    private String type;

    private String targetType;

    private String targetId;

    private String snippet;

    private Boolean isRead;

    private LocalDateTime createdAt;

    // 以下字段由查询关联得到，不写入通知表
    private String actorName;

    private String actorAvatarUrl;

    private String targetTitle;

    private String targetAlbumId;
}
