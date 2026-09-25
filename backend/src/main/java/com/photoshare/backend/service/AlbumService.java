package com.photoshare.backend.service;

import com.photoshare.backend.dto.AlbumDTO;
import com.photoshare.backend.entity.Album;

import java.util.List;

public interface AlbumService {

    Album createAlbum(AlbumDTO dto, String userId);

    List<Album> listAlbums(String userId);

    Album getAlbumById(String id, String viewerId, String viewerRole);

    Album updateAlbum(String id, AlbumDTO dto, String userId);

    void deleteAlbum(String id, String userId);

    List<Album> listDeletedAlbums(String userId);

    void restoreAlbum(String id, String userId);

    void permanentlyDeleteAlbum(String id, String userId);
}
