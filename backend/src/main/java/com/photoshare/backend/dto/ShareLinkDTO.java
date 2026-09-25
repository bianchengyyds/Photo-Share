package com.photoshare.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShareLinkDTO {

    @NotBlank(message = "相册ID不能为空")
    private String albumId;

    private String password;

    private LocalDateTime expireAt;

    private Boolean allowDownload = true;

    // 是否允许协作者上传照片，默认开启
    private Boolean allowUpload = true;
}
