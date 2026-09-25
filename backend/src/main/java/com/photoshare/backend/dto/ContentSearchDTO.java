package com.photoshare.backend.dto;

import lombok.Data;

/**
 * 相册与照片的筛选条件。getter 统一把缺省值归一成空串，Mapper 里只需判断 = ''，
 * 既避免向 JDBC 传 null，也让"无此条件"和 SQL 分支一一对应。
 */
@Data
public class ContentSearchDTO {

    private static final int DEFAULT_SIZE = 20;

    private static final int MAX_SIZE = 100;

    private String keyword;

    private String tagId;

    private String albumId;

    private String ownerId;

    /** 起始日期，格式 yyyy-MM-dd */
    private String startDate;

    /** 截止日期，格式 yyyy-MM-dd，含当天 */
    private String endDate;

    private Integer page;

    private Integer size;

    public String getKeyword() {
        return keyword == null ? "" : keyword.trim();
    }

    public String getTagId() {
        return tagId == null ? "" : tagId.trim();
    }

    public String getAlbumId() {
        return albumId == null ? "" : albumId.trim();
    }

    public String getOwnerId() {
        return ownerId == null ? "" : ownerId.trim();
    }

    public String getStartDate() {
        return startDate == null ? "" : startDate.trim();
    }

    public String getEndDate() {
        return endDate == null ? "" : endDate.trim();
    }

    public int getPage() {
        return page == null || page < 1 ? 1 : page;
    }

    public int getSize() {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public int getOffset() {
        return (getPage() - 1) * getSize();
    }
}
