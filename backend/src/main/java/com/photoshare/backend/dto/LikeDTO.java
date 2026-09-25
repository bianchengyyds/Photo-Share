package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LikeDTO {

    @NotBlank(message = "点赞目标类型不能为空")
    private String targetType;

    @NotBlank(message = "点赞目标ID不能为空")
    private String targetId;
}
