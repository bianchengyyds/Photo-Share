package com.photoshare.backend.service;

import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;

import java.util.List;

public interface SearchService {

    List<Album> searchAlbums(ContentSearchDTO query, String viewerId);

    List<Photo> searchPhotos(ContentSearchDTO query, String viewerId);

    List<Album> listFollowedAlbums(String viewerId, int page, int size);
}
