package com.photoshare.backend.entity;

import com.photoshare.backend.dto.TagViewDTO;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Photo {

    private String id;

    private String albumId;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private String mimeType; // 文件类型

    private Integer sortOrder; // 排序顺序

    private LocalDateTime createdAt;

    private Boolean isDeleted;

    private String albumTitle;

    private String userId;

    private String status; // 审核状态：pending(待审核)、approved(已通过)、rejected(已拒绝)

    private String reviewedBy; // 审核人ID

    private LocalDateTime reviewedAt; // 审核时间

    private String rejectReason; // 拒绝原因

    private String uploaderName; // 上传者昵称（用于管理员审核页面）

    private String uploaderNickname; // 协作者通过分享链接上传时填写的昵称

    private Integer likeCount; // 实时点赞数

    private List<TagViewDTO> tags; // 照片标签
}
