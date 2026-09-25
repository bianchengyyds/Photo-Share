package com.photoshare.backend.entity;

import com.photoshare.backend.dto.TagViewDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Album {

    private String id;

    private String title;

    private String description;

    private String coverUrl;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isDeleted;

    private String userId;

    private String visibility;

    // 以下字段由查询关联或服务层填充，不写入相册表
    private String uploaderName;

    private Integer likeCount;

    private Integer commentCount;

    private List<TagViewDTO> tags;
}
