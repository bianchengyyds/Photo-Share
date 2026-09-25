package com.photoshare.backend.mapper;

import com.photoshare.backend.entity.Notification;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface NotificationMapper {

    // 照片类事件只存照片ID，跳转用的相册ID在这里 JOIN 出来，避免通知表冗余字段
    String COLUMNS = "n.*, COALESCE(u.nickname, u.username) AS actorName, u.avatar_url AS actorAvatarUrl, " +
            "COALESCE(a.title, pa.title) AS targetTitle, " +
            "CASE WHEN n.target_type = 'ALBUM' THEN n.target_id " +
            "     WHEN n.target_type = 'PHOTO' THEN p.album_id END AS targetAlbumId";

    String JOINS = " FROM notification n " +
            "LEFT JOIN `user` u ON u.id = n.actor_id " +
            "LEFT JOIN album a ON n.target_type = 'ALBUM' AND a.id = n.target_id " +
            "LEFT JOIN photo p ON n.target_type = 'PHOTO' AND p.id = n.target_id " +
            "LEFT JOIN album pa ON pa.id = p.album_id";

    @Insert("INSERT INTO notification (id, user_id, actor_id, type, target_type, target_id, snippet) " +
            "VALUES (#{id}, #{userId}, #{actorId}, #{type}, #{targetType}, #{targetId}, #{snippet})")
    int insert(Notification notification);

    @Select("SELECT " + COLUMNS + JOINS + " WHERE n.user_id = #{userId} " +
            "ORDER BY n.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Notification> selectByUser(@Param("userId") String userId,
                                    @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT " + COLUMNS + JOINS + " WHERE n.user_id = #{userId} AND n.is_read = 0 " +
            "ORDER BY n.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<Notification> selectUnreadByUser(@Param("userId") String userId,
                                          @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(String userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND id = #{id}")
    int markRead(@Param("userId") String userId, @Param("id") String id);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllRead(String userId);

    @Delete("DELETE FROM notification WHERE user_id = #{userId} AND id = #{id}")
    int deleteByIdAndUser(@Param("userId") String userId, @Param("id") String id);

    // 取消点赞、取关时按事件删除，避免留下点不开的红点
    @Delete("DELETE FROM notification WHERE actor_id = #{actorId} AND type = #{type} " +
            "AND target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByEvent(@Param("actorId") String actorId, @Param("type") String type,
                      @Param("targetType") String targetType, @Param("targetId") String targetId);
}
