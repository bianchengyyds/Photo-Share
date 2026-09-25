package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserLike {

    private String id;

    private String userId;

    // ALBUM=相册，PHOTO=照片
    private String targetType;

    private String targetId;

    private LocalDateTime createdAt;
}
