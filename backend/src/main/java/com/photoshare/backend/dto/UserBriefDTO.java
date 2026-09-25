package com.photoshare.backend.dto;

import lombok.Data;

@Data
public class UserBriefDTO {

    private String id;

    private String username;

    private String nickname;

    private String avatarUrl;

    // 当前查看者是否已关注这个人，未登录时为空
    private Boolean followed;
}
