package com.photoshare.backend.dto;

import lombok.Data;

@Data
public class TagViewDTO {

    // tag_relation 主键，删除自己添加的标签时使用
    private String relationId;

    private String name;

    // 添加该标签的用户ID
    private String userId;
}
