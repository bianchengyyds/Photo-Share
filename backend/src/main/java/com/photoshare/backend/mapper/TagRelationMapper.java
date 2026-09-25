package com.photoshare.backend.mapper;

import com.photoshare.backend.entity.TagRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagRelationMapper {

    String TAG_COLUMNS = "tr.id, tr.tag_id AS tagId, tr.user_id AS userId, tr.target_type AS targetType, " +
            "tr.target_id AS targetId, tr.created_at AS createdAt, t.name AS tagName";

    @Insert("INSERT INTO tag_relation (id, tag_id, user_id, target_type, target_id) VALUES (#{id}, #{tagId}, #{userId}, #{targetType}, #{targetId})")
    int insert(TagRelation tagRelation);

    @Select("SELECT * FROM tag_relation WHERE id = #{id}")
    TagRelation selectById(String id);

    // 只能删除自己添加的那一条，非本人添加时影响行数为 0
    @Delete("DELETE FROM tag_relation WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUser(@Param("id") String id, @Param("userId") String userId);

    @Select("SELECT * FROM tag_relation WHERE tag_id = #{tagId} AND target_type = #{targetType} AND target_id = #{targetId} AND user_id = #{userId} LIMIT 1")
    TagRelation selectByTagAndTarget(@Param("tagId") String tagId, @Param("targetType") String targetType,
                                     @Param("targetId") String targetId, @Param("userId") String userId);

    @Select("SELECT " + TAG_COLUMNS + " FROM tag_relation tr JOIN tag t ON t.id = tr.tag_id " +
            "WHERE tr.target_type = #{targetType} AND tr.target_id = #{targetId} ORDER BY tr.created_at ASC")
    List<TagRelation> selectByTarget(@Param("targetType") String targetType, @Param("targetId") String targetId);

    // 一次取出某个相册（相册本身 + 相册内所有照片）的全部标签，避免逐张照片查询
    @Select("SELECT " + TAG_COLUMNS + " FROM tag_relation tr JOIN tag t ON t.id = tr.tag_id " +
            "WHERE (tr.target_type = 'ALBUM' AND tr.target_id = #{albumId}) " +
            "OR (tr.target_type = 'PHOTO' AND tr.target_id IN (SELECT id FROM photo WHERE album_id = #{albumId})) " +
            "ORDER BY tr.created_at ASC")
    List<TagRelation> selectTagsForAlbum(String albumId);

    // 首页：一次取出我名下所有相册的相册级标签
    @Select("SELECT " + TAG_COLUMNS + " FROM tag_relation tr JOIN tag t ON t.id = tr.tag_id " +
            "JOIN album a ON a.id = tr.target_id " +
            "WHERE tr.target_type = 'ALBUM' AND a.user_id = #{userId} AND a.is_deleted = 0 ORDER BY tr.created_at ASC")
    List<TagRelation> selectTagsForOwnerAlbums(String userId);
}
