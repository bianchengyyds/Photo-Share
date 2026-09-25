package com.photoshare.backend.mapper;

import com.photoshare.backend.dto.UserBriefDTO;
import com.photoshare.backend.entity.UserFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFollowMapper {

    String BRIEF_COLUMNS = "u.id, u.username, COALESCE(u.nickname, u.username) AS nickname, u.avatar_url AS avatarUrl, " +
            "EXISTS(SELECT 1 FROM user_follow x WHERE x.follower_id = #{viewerId} AND x.followee_id = u.id) AS followed";

    @Insert("INSERT INTO user_follow (id, follower_id, followee_id) VALUES (#{id}, #{followerId}, #{followeeId})")
    int insert(UserFollow userFollow);

    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    int delete(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    int exists(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE followee_id = #{userId}")
    int countFollowers(String userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    int countFollowing(String userId);

    // 粉丝列表：按关注时间倒序，附带"我是否也关注了TA"
    @Select("SELECT " + BRIEF_COLUMNS + " FROM user_follow f JOIN `user` u ON u.id = f.follower_id " +
            "WHERE f.followee_id = #{userId} ORDER BY f.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<UserBriefDTO> selectFollowers(@Param("userId") String userId, @Param("viewerId") String viewerId,
                                       @Param("limit") int limit, @Param("offset") int offset);

    // 关注列表：按关注时间倒序
    @Select("SELECT " + BRIEF_COLUMNS + " FROM user_follow f JOIN `user` u ON u.id = f.followee_id " +
            "WHERE f.follower_id = #{userId} ORDER BY f.created_at DESC LIMIT #{limit} OFFSET #{offset}")
    List<UserBriefDTO> selectFollowing(@Param("userId") String userId, @Param("viewerId") String viewerId,
                                       @Param("limit") int limit, @Param("offset") int offset);
}
