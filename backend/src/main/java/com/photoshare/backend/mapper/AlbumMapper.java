package com.photoshare.backend.mapper;

import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.entity.Album;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AlbumMapper {

    // 关注关系即授权：本人相册一律可见，他人相册必须设为 PUBLIC 且被查看者关注
    String VISIBLE_SCOPE = " AND (a.user_id = #{viewerId} " +
            "OR (a.visibility = 'PUBLIC' AND a.user_id IN " +
            "(SELECT followee_id FROM user_follow WHERE follower_id = #{viewerId}))) ";

    @Insert("INSERT INTO album (id, title, description, cover_url, user_id, visibility) VALUES (#{id}, #{title}, #{description}, #{coverUrl}, #{userId}, #{visibility})")
    int insert(Album album);

    @Select("SELECT album.*, " +
            "(SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'ALBUM' AND ul.target_id = album.id) AS likeCount, " +
            "(SELECT COUNT(*) FROM album_comment ac WHERE ac.album_id = album.id AND ac.is_deleted = 0) AS commentCount " +
            "FROM album WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Album> selectByUserId(String userId);

    @Select("SELECT album.*, " +
            "(SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'ALBUM' AND ul.target_id = album.id) AS likeCount, " +
            "(SELECT COUNT(*) FROM album_comment ac WHERE ac.album_id = album.id AND ac.is_deleted = 0) AS commentCount " +
            "FROM album WHERE id = #{id} AND is_deleted = 0")
    Album selectById(String id);

    @Select("SELECT * FROM album WHERE id = #{id}")
    Album selectByIdIncludeDeleted(String id);

    @Update("UPDATE album SET title = #{title}, description = #{description}, visibility = #{visibility} WHERE id = #{id}")
    int update(Album album);

    @Update("UPDATE album SET is_deleted = 1 WHERE id = #{id}")
    int softDeleteById(String id);

    @Update("UPDATE album SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(String id);

    @Delete("DELETE FROM album WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT * FROM album WHERE user_id = #{userId} AND is_deleted = 1 ORDER BY updated_at DESC")
    List<Album> selectDeletedByUserId(String userId);

    @Select("SELECT COUNT(*) FROM album WHERE user_id = #{userId} AND is_deleted = 0")
    int countByUserId(String userId);

    @Select("SELECT COUNT(*) FROM album WHERE user_id = #{userId} AND is_deleted = 0 AND visibility = 'PUBLIC'")
    int countPublicByUserId(String userId);

    // 空串表示该条件不生效，由 ContentSearchDTO 的 getter 统一归一，避免向 JDBC 传 null
    @Select("SELECT a.*, " +
            "(SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'ALBUM' AND ul.target_id = a.id) AS likeCount, " +
            "(SELECT COUNT(*) FROM album_comment ac WHERE ac.album_id = a.id AND ac.is_deleted = 0) AS commentCount " +
            "FROM album a WHERE a.is_deleted = 0" + VISIBLE_SCOPE +
            "AND (#{q.keyword} = '' OR a.title LIKE CONCAT('%', #{q.keyword}, '%') " +
            "OR a.description LIKE CONCAT('%', #{q.keyword}, '%') " +
            "OR EXISTS (SELECT 1 FROM tag_relation tr JOIN tag t ON t.id = tr.tag_id " +
            "WHERE tr.target_type = 'ALBUM' AND tr.target_id = a.id AND t.name LIKE CONCAT('%', #{q.keyword}, '%'))) " +
            "AND (#{q.tagId} = '' OR EXISTS (SELECT 1 FROM tag_relation tr2 " +
            "WHERE tr2.target_type = 'ALBUM' AND tr2.target_id = a.id AND tr2.tag_id = #{q.tagId})) " +
            "AND (#{q.ownerId} = '' OR a.user_id = #{q.ownerId}) " +
            // MySQL 会把 CONCAT('', ' 00:00:00') 折叠成非法 DATETIME 常量，空值必须在 STR_TO_DATE 里变 NULL
            "AND (#{q.startDate} = '' OR a.created_at >= STR_TO_DATE(#{q.startDate}, '%Y-%m-%d')) " +
            "AND (#{q.endDate} = '' OR a.created_at < DATE_ADD(STR_TO_DATE(#{q.endDate}, '%Y-%m-%d'), INTERVAL 1 DAY)) " +
            "ORDER BY a.created_at DESC LIMIT #{q.size} OFFSET #{q.offset}")
    List<Album> searchAlbums(@Param("q") ContentSearchDTO q, @Param("viewerId") String viewerId);

    // 动态：只看我关注的人公开出来的相册，本人相册不混进来，因此不复用 VISIBLE_SCOPE
    @Select("SELECT a.*, COALESCE(u.nickname, u.username) AS uploaderName, " +
            "(SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'ALBUM' AND ul.target_id = a.id) AS likeCount, " +
            "(SELECT COUNT(*) FROM album_comment ac WHERE ac.album_id = a.id AND ac.is_deleted = 0) AS commentCount " +
            "FROM album a JOIN user u ON u.id = a.user_id " +
            "WHERE a.is_deleted = 0 AND a.visibility = 'PUBLIC' " +
            "AND a.user_id IN (SELECT followee_id FROM user_follow WHERE follower_id = #{viewerId}) " +
            "ORDER BY a.created_at DESC LIMIT #{size} OFFSET #{offset}")
    List<Album> selectFollowingAlbums(@Param("viewerId") String viewerId,
                                      @Param("size") int size,
                                      @Param("offset") int offset);
}
