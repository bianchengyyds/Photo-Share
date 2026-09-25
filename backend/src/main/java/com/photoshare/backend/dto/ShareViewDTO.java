package com.photoshare.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分享页视图：面向访客（游客/登录成员/相册所有者）的脱敏返回，
 * 不携带访问密码，只说明"需要密码"以及当前身份能做什么。
 */
@Data
public class ShareViewDTO {

    private String shareCode;

    // 密码未通过校验时为 null，避免向未授权访客泄漏相册内容
    private String albumTitle;

    private String albumDescription;

    // 登录访客调用点赞/评论/标签接口时需要用到
    private String albumId;

    private Integer albumLikeCount;

    private Integer albumCommentCount;

    private List<TagViewDTO> albumTags;

    // 该链接是否设置了访问密码
    private Boolean requirePassword;

    // 密码校验已通过（或本就无需密码），可以看到照片内容
    private Boolean canView;

    // 游客与登录成员均可通过链接投稿（仍需管理员审核）
    private Boolean canContribute;

    // 仅登录成员可获得：修改相册信息、删除相册内照片
    private Boolean canEdit;

    private Boolean allowDownload;

    // 当前身份：GUEST（未登录）、OWNER（相册所有者）、MEMBER（被链接授权的登录用户）、VIEWER
    private String myPermission;

    private LocalDateTime expireAt;
}
