package com.photoshare.backend.service;

import com.photoshare.backend.dto.LikeSummaryDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;

import java.util.List;

public interface LikeService {

    void like(String targetType, String targetId, String userId);

    void unlike(String targetType, String targetId, String userId);

    LikeSummaryDTO getMyLikes(String userId);

    List<Album> listLikedAlbums(String userId);

    List<Photo> listLikedPhotos(String userId);
}
