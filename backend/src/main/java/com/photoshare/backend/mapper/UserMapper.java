package com.photoshare.backend.mapper;

import com.photoshare.backend.dto.UserBriefDTO;
import com.photoshare.backend.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    String BRIEF_COLUMNS = "u.id, u.username, COALESCE(u.nickname, u.username) AS nickname, u.avatar_url AS avatarUrl, " +
            "EXISTS(SELECT 1 FROM user_follow x WHERE x.follower_id = #{viewerId} AND x.followee_id = u.id) AS followed";

    @Insert("INSERT INTO user (id, username, password, nickname, role) VALUES (#{id}, #{username}, #{password}, #{nickname}, #{role})")
    int insert(User user);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User selectByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id}")
    User selectById(String id);

    @Select("SELECT id, username, COALESCE(nickname, username) AS nickname, avatar_url AS avatarUrl FROM `user` WHERE id = #{id}")
    UserBriefDTO selectBriefById(String id);

    // @提及候选与关注搜索：管理员是系统账号不外露，也不把查看者自己列进去
    @Select("SELECT " + BRIEF_COLUMNS + " FROM `user` u " +
            "WHERE u.role <> 'ADMIN' AND u.id <> #{viewerId} " +
            "AND (u.username LIKE CONCAT('%', #{keyword}, '%') OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY u.username ASC LIMIT #{limit}")
    List<UserBriefDTO> selectSuggested(@Param("keyword") String keyword, @Param("viewerId") String viewerId,
                                       @Param("limit") int limit);
}
