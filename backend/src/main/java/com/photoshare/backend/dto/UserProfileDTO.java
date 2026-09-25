package com.photoshare.backend.dto;

import com.photoshare.backend.entity.Album;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserProfileDTO {

    private String id;

    private String username;

    private String nickname;

    private String avatarUrl;

    private LocalDateTime createdAt;

    private Integer albumCount;

    private Integer followerCount;

    private Integer followingCount;

    // 查看者能否看到 TA 的相册内容：本人，或已关注且相册设为公开
    private Boolean canViewContent;

    // 查看者与被查看者的关系标记，用于渲染关注按钮
    private Boolean followedByMe;

    private Boolean self;

    private List<Album> albums;
}
