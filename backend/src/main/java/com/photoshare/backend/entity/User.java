package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {

    private String id;

    private String username;

    private String password;

    private String nickname;

    private String avatarUrl;

    private String role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
