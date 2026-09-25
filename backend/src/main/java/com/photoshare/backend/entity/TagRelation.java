package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TagRelation {

    private String id;

    private String tagId;

    private String userId;

    // ALBUM=相册，PHOTO=照片
    private String targetType;

    private String targetId;

    private LocalDateTime createdAt;

    // 由关联查询 tag 表得到
    private String tagName;
}
