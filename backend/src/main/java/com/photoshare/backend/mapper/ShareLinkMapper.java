package com.photoshare.backend.mapper;

import com.photoshare.backend.entity.ShareLink;
import org.apache.ibatis.annotations.*;

@Mapper
public interface ShareLinkMapper {

    @Insert("INSERT INTO share_link (id, album_id, share_code, password, expire_at, allow_download, allow_upload, visit_count, user_id) VALUES (#{id}, #{albumId}, #{shareCode}, #{password}, #{expireAt}, #{allowDownload}, #{allowUpload}, #{visitCount}, #{userId})")
    int insert(ShareLink shareLink);

    @Select("SELECT * FROM share_link WHERE share_code = #{shareCode}")
    ShareLink selectByShareCode(String shareCode);

    @Update("UPDATE share_link SET visit_count = #{visitCount} WHERE id = #{id}")
    int updateVisitCount(ShareLink shareLink);

    @Select("SELECT * FROM share_link WHERE album_id = #{albumId}")
    ShareLink selectByAlbumId(String albumId);
}
