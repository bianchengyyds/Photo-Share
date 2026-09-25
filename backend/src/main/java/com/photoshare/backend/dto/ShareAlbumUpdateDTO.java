package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录成员通过分享链接修改相册信息
 */
@Data
public class ShareAlbumUpdateDTO {

    // 访问密码：分享链接设置了密码时必须携带
    private String password;

    @NotBlank(message = "相册标题不能为空")
    private String title;

    private String description;
}
