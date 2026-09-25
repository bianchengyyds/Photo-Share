package com.photoshare.backend.mapper;

import com.photoshare.backend.entity.AlbumComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlbumCommentMapper {

    @Insert("INSERT INTO album_comment (id, album_id, parent_id, reply_to_user_id, user_id, content) " +
            "VALUES (#{id}, #{albumId}, #{parentId}, #{replyToUserId}, #{userId}, #{content})")
    int insert(AlbumComment comment);

    @Select("SELECT c.*, COALESCE(u.nickname, u.username) AS userName, u.avatar_url AS avatarUrl, " +
            "COALESCE(ru.nickname, ru.username) AS replyToUserName " +
            "FROM album_comment c LEFT JOIN `user` u ON c.user_id = u.id " +
            "LEFT JOIN `user` ru ON ru.id = c.reply_to_user_id " +
            "WHERE c.album_id = #{albumId} AND c.is_deleted = 0 ORDER BY c.created_at DESC")
    List<AlbumComment> selectByAlbumId(String albumId);

    @Select("SELECT c.*, COALESCE(u.nickname, u.username) AS userName, u.avatar_url AS avatarUrl, " +
            "COALESCE(ru.nickname, ru.username) AS replyToUserName " +
            "FROM album_comment c LEFT JOIN `user` u ON c.user_id = u.id " +
            "LEFT JOIN `user` ru ON ru.id = c.reply_to_user_id WHERE c.id = #{id}")
    AlbumComment selectById(String id);

    @Update("UPDATE album_comment SET is_deleted = 1 WHERE id = #{id}")
    int softDeleteById(String id);

    /** 删一级评论时连带软删它下面的一级回复，否则回复会变成没有归属的孤儿 */
    @Update("UPDATE album_comment SET is_deleted = 1 WHERE parent_id = #{parentId} AND is_deleted = 0")
    int softDeleteByParentId(String parentId);
}
