package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagDTO {

    @NotBlank(message = "标签目标类型不能为空")
    private String targetType;

    @NotBlank(message = "标签目标ID不能为空")
    private String targetId;

    @NotBlank(message = "标签名不能为空")
    @Size(max = 50, message = "标签名不能超过50字")
    private String name;
}
