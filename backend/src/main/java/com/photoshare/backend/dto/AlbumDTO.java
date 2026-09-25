package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlbumDTO {

    @NotBlank(message = "相册标题不能为空")
    private String title;

    private String description;

    /** 可见范围：PUBLIC=关注者可见，PRIVATE=仅本人；留空按 PRIVATE 处理 */
    private String visibility;
}
