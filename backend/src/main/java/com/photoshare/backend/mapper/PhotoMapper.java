package com.photoshare.backend.mapper;

import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.entity.Photo;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PhotoMapper {

    @Insert("INSERT INTO photo (id, album_id, file_name, file_url, file_size, mime_type, sort_order, user_id, status, uploader_nickname) VALUES (#{id}, #{albumId}, #{fileName}, #{fileUrl}, #{fileSize}, #{mimeType}, #{sortOrder}, #{userId}, #{status}, #{uploaderNickname})")
    int insert(Photo photo);

    @Select("SELECT photo.*, (SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'PHOTO' AND ul.target_id = photo.id) AS likeCount " +
            "FROM photo WHERE album_id = #{albumId} AND is_deleted = 0 AND status = 'approved' ORDER BY sort_order ASC, created_at DESC")
    List<Photo> selectApprovedByAlbumId(String albumId);

    // 相册所有者视角：自己的照片（含待审核、已拒绝，用于查看审核进度）加上相册内已通过的照片
    @Select("SELECT photo.*, (SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'PHOTO' AND ul.target_id = photo.id) AS likeCount " +
            "FROM photo WHERE album_id = #{albumId} AND is_deleted = 0 AND (status = 'approved' OR user_id = #{userId}) ORDER BY sort_order ASC, created_at DESC")
    List<Photo> selectByAlbumIdForOwner(@Param("albumId") String albumId, @Param("userId") String userId);

    @Select("SELECT photo.*, (SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'PHOTO' AND ul.target_id = photo.id) AS likeCount " +
            "FROM photo WHERE album_id = #{albumId} AND is_deleted = 0 ORDER BY sort_order ASC, created_at DESC")
    List<Photo> selectByAlbumId(String albumId);

    @Select("SELECT * FROM photo WHERE id = #{id} AND is_deleted = 0")
    Photo selectById(String id);

    @Select("SELECT * FROM photo WHERE id = #{id}")
    Photo selectByIdIncludeDeleted(String id);

    @Update("UPDATE photo SET is_deleted = 1 WHERE id = #{id}")
    int softDeleteById(String id);

    @Update("UPDATE photo SET is_deleted = 0 WHERE id = #{id}")
    int restoreById(String id);

    @Update("UPDATE photo SET is_deleted = 1 WHERE album_id = #{albumId}")
    int softDeleteByAlbumId(String albumId);

    @Delete("DELETE FROM photo WHERE id = #{id}")
    int deleteById(String id);

    @Select("SELECT p.*, a.title AS albumTitle FROM photo p LEFT JOIN album a ON p.album_id = a.id WHERE p.user_id = #{userId} AND p.is_deleted = 1 ORDER BY p.created_at DESC")
    List<Photo> selectDeletedByUserId(String userId);

    @Select("SELECT * FROM photo WHERE album_id = #{albumId}")
    List<Photo> selectByAlbumIdIncludeDeleted(String albumId);

    @Select("SELECT * FROM photo WHERE album_id = #{albumId} AND is_deleted = 1")
    List<Photo> selectDeletedByAlbumId(String albumId);

    @Update("UPDATE photo SET is_deleted = 0 WHERE album_id = #{albumId}")
    int restoreByAlbumId(String albumId);

    // 管理员审核相关方法
    @Select("SELECT p.*, a.title AS albumTitle, COALESCE(p.uploader_nickname, u.nickname) AS uploaderName FROM photo p LEFT JOIN album a ON p.album_id = a.id LEFT JOIN user u ON p.user_id = u.id WHERE p.status = 'pending' AND p.is_deleted = 0 ORDER BY p.created_at DESC")
    List<Photo> selectPendingPhotos();

    @Select("SELECT p.*, a.title AS albumTitle, COALESCE(p.uploader_nickname, u.nickname) AS uploaderName FROM photo p LEFT JOIN album a ON p.album_id = a.id LEFT JOIN user u ON p.user_id = u.id WHERE p.is_deleted = 0 ORDER BY p.created_at DESC")
    List<Photo> selectAllPhotosForAdmin();

    @Select("SELECT p.*, a.title AS albumTitle, COALESCE(p.uploader_nickname, u.nickname) AS uploaderName FROM photo p LEFT JOIN album a ON p.album_id = a.id LEFT JOIN user u ON p.user_id = u.id WHERE p.status = #{status} AND p.is_deleted = 0 ORDER BY p.created_at DESC")
    List<Photo> selectPhotosByStatus(String status);

    @Update("UPDATE photo SET file_url = #{fileUrl}, status = #{status}, reviewed_by = #{reviewedBy}, reviewed_at = #{reviewedAt}, reject_reason = #{rejectReason} WHERE id = #{id}")
    int updateReviewStatus(Photo photo);

    // 照片搜索：只覆盖本人 + 我关注的公开相册，空串条件表示该筛选项不生效
    @Select("SELECT p.*, a.title AS albumTitle, COALESCE(u.nickname, u.username) AS uploaderName, " +
            "(SELECT COUNT(*) FROM user_like ul WHERE ul.target_type = 'PHOTO' AND ul.target_id = p.id) AS likeCount " +
            "FROM photo p JOIN album a ON a.id = p.album_id LEFT JOIN `user` u ON u.id = p.user_id " +
            "WHERE p.is_deleted = 0 AND p.status = 'approved' AND a.is_deleted = 0" + AlbumMapper.VISIBLE_SCOPE +
            "AND (#{q.albumId} = '' OR p.album_id = #{q.albumId}) " +
            "AND (#{q.ownerId} = '' OR a.user_id = #{q.ownerId}) " +
            "AND (#{q.keyword} = '' OR p.file_name LIKE CONCAT('%', #{q.keyword}, '%') " +
            "OR a.title LIKE CONCAT('%', #{q.keyword}, '%') " +
            "OR a.description LIKE CONCAT('%', #{q.keyword}, '%') " +
            "OR EXISTS (SELECT 1 FROM tag_relation tr JOIN tag t ON t.id = tr.tag_id " +
            "WHERE t.name LIKE CONCAT('%', #{q.keyword}, '%') " +
            "AND ((tr.target_type = 'PHOTO' AND tr.target_id = p.id) OR (tr.target_type = 'ALBUM' AND tr.target_id = p.album_id)))) " +
            "AND (#{q.tagId} = '' OR EXISTS (SELECT 1 FROM tag_relation tr2 " +
            "WHERE tr2.tag_id = #{q.tagId} " +
            "AND ((tr2.target_type = 'PHOTO' AND tr2.target_id = p.id) OR (tr2.target_type = 'ALBUM' AND tr2.target_id = p.album_id)))) " +
            // MySQL 会把 CONCAT('', ' 00:00:00') 折叠成非法 DATETIME 常量，空值必须在 STR_TO_DATE 里变 NULL
            "AND (#{q.startDate} = '' OR p.created_at >= STR_TO_DATE(#{q.startDate}, '%Y-%m-%d')) " +
            "AND (#{q.endDate} = '' OR p.created_at < DATE_ADD(STR_TO_DATE(#{q.endDate}, '%Y-%m-%d'), INTERVAL 1 DAY)) " +
            "ORDER BY p.created_at DESC LIMIT #{q.size} OFFSET #{q.offset}")
    List<Photo> selectVisiblePhotos(@Param("q") ContentSearchDTO q, @Param("viewerId") String viewerId);
}
