package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareLink {

    private String id;

    private String albumId;

    private String shareCode;

    private String password;

    private LocalDateTime expireAt;

    private Boolean allowDownload;

    // 是否允许协作者通过该链接上传照片
    private Boolean allowUpload;

    private Integer visitCount;

    private LocalDateTime createdAt;

    private String userId;
}
