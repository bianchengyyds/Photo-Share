package com.photoshare.backend.dto;

import lombok.Data;

/** 搜索页的标签候选：标签本身加上被使用的次数，用于按热度排序 */
@Data
public class TagCountDTO {

    private String id;

    private String name;

    private int useCount;
}
