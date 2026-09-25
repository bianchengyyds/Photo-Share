package com.photoshare.backend.service.impl;

import com.photoshare.backend.entity.Album;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.UserFollowMapper;
import com.photoshare.backend.service.ContentAccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContentAccessServiceImpl implements ContentAccessService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    public boolean canView(Album album, String viewerId, String viewerRole) {
        if (album == null) {
            return false;
        }
        if ("ADMIN".equals(viewerRole)) {
            return true;
        }
        if (viewerId != null && viewerId.equals(album.getUserId())) {
            return true;
        }
        // 粉丝授权：只有作者设为公开的相册才会被关注者看到
        return "PUBLIC".equals(album.getVisibility())
                && viewerId != null
                && userFollowMapper.exists(viewerId, album.getUserId()) > 0;
    }

    @Override
    public Album requireViewable(String albumId, String viewerId, String viewerRole) {
        Album album = albumMapper.selectById(albumId);
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (!canView(album, viewerId, viewerRole)) {
            throw new RuntimeException("相册未公开，仅作者及其关注者可见");
        }
        return album;
    }
}
