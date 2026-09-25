package com.photoshare.backend.mapper;

import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.UserLike;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserLikeMapper {

    @Insert("INSERT INTO user_like (id, user_id, target_type, target_id) VALUES (#{id}, #{userId}, #{targetType}, #{targetId})")
    int insert(UserLike userLike);

    @Delete("DELETE FROM user_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int delete(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);

    @Select("SELECT COUNT(*) FROM user_like WHERE user_id = #{userId} AND target_type = #{targetType} AND target_id = #{targetId}")
    int exists(@Param("userId") String userId, @Param("targetType") String targetType, @Param("targetId") String targetId);

    @Select("SELECT COUNT(*) FROM user_like WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int countByTarget(@Param("targetType") String targetType, @Param("targetId") String targetId);

    @Select("SELECT target_id FROM user_like WHERE user_id = #{userId} AND target_type = 'ALBUM'")
    List<String> selectLikedAlbumIds(String userId);

    @Select("SELECT target_id FROM user_like WHERE user_id = #{userId} AND target_type = 'PHOTO'")
    List<String> selectLikedPhotoIds(String userId);

    // 我点赞过的相册：按点赞时间倒序，附带实时点赞数
    @Select("SELECT a.*, (SELECT COUNT(*) FROM user_like x WHERE x.target_type = 'ALBUM' AND x.target_id = a.id) AS likeCount " +
            "FROM user_like ul JOIN album a ON a.id = ul.target_id " +
            "WHERE ul.user_id = #{userId} AND ul.target_type = 'ALBUM' AND a.is_deleted = 0 " +
            "ORDER BY ul.created_at DESC")
    List<Album> selectLikedAlbums(String userId);

    // 我点赞过的照片：相册本身被删除时不再出现
    @Select("SELECT p.*, a.title AS albumTitle, (SELECT COUNT(*) FROM user_like x WHERE x.target_type = 'PHOTO' AND x.target_id = p.id) AS likeCount " +
            "FROM user_like ul JOIN photo p ON p.id = ul.target_id LEFT JOIN album a ON a.id = p.album_id " +
            "WHERE ul.user_id = #{userId} AND ul.target_type = 'PHOTO' AND p.is_deleted = 0 AND a.is_deleted = 0 " +
            "ORDER BY ul.created_at DESC")
    List<Photo> selectLikedPhotos(String userId);
}
