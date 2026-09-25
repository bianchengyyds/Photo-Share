package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFollow {

    private String id;

    private String followerId;

    private String followeeId;

    private LocalDateTime createdAt;
}
