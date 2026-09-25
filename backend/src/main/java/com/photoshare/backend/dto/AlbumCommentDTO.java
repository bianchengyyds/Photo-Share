package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AlbumCommentDTO {

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容不能超过500字")
    private String content;

    /** 回复某条评论时带上它的ID，留空表示发表一级评论 */
    private String parentId;

    /** 正文里 @ 到的人，由前端选人面板给出，服务端逐个校验存在性 */
    private List<String> mentionUserIds;
}
