package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 分享链接下发表评论：先校验链接密码，再按相册评论的同一套规则落库 */
@Data
public class ShareCommentDTO {

    private String password;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /** 回复某条评论时带上它的ID，留空表示发表一级评论 */
    private String parentId;

    /** 正文里 @ 到的人，服务端逐个校验存在性 */
    private List<String> mentionUserIds;
}
