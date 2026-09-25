package com.photoshare.backend.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Tag {

    private String id;

    private String name;

    private LocalDateTime createdAt;
}
