package com.photoshare.backend.mapper;

import com.photoshare.backend.dto.TagCountDTO;
import com.photoshare.backend.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper {

    @Insert("INSERT INTO tag (id, name) VALUES (#{id}, #{name})")
    int insert(Tag tag);

    // uk_name 使用 utf8mb4_unicode_ci，天然不区分大小写
    @Select("SELECT * FROM tag WHERE name = #{name} LIMIT 1")
    Tag selectByName(String name);

    // 只返回真正被用过至少一次的标签
    @Select("SELECT t.id AS id, t.name AS name, COUNT(r.id) AS useCount " +
            "FROM tag t JOIN tag_relation r ON r.tag_id = t.id " +
            "GROUP BY t.id, t.name ORDER BY useCount DESC, t.name ASC LIMIT #{limit}")
    List<TagCountDTO> selectPopular(@Param("limit") int limit);
}
