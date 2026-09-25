package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.AlbumDTO;
import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.service.AlbumService;
import com.photoshare.backend.service.ContentAccessService;
import com.photoshare.backend.service.TagService;
import com.photoshare.backend.storage.PendingStorageService;
import com.photoshare.backend.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PendingStorageService pendingStorageService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ContentAccessService contentAccessService;

    @Override
    public Album createAlbum(AlbumDTO dto, String userId) {
        Album album = new Album();
        album.setId(UUID.randomUUID().toString());
        album.setTitle(dto.getTitle());
        album.setDescription(dto.getDescription());
        album.setUserId(userId);
        album.setVisibility(normalizeVisibility(dto.getVisibility()));
        albumMapper.insert(album);
        return album;
    }

    @Override
    public List<Album> listAlbums(String userId) {
        List<Album> albums = albumMapper.selectByUserId(userId);
        Map<String, List<TagViewDTO>> tagMap = tagService.mapTagsForOwnerAlbums(userId);
        for (Album album : albums) {
            album.setTags(tagMap.getOrDefault(album.getId(), new ArrayList<>()));
        }
        return albums;
    }

    @Override
    public Album getAlbumById(String id, String viewerId, String viewerRole) {
        Album album = contentAccessService.requireViewable(id, viewerId, viewerRole);
        album.setTags(tagService.listTags("ALBUM", id));
        return album;
    }

    @Override
    public Album updateAlbum(String id, AlbumDTO dto, String userId) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此相册");
        }
        album.setTitle(dto.getTitle());
        album.setDescription(dto.getDescription());
        // 未显式提交可见性时保持原值，只改标题描述不该把相册悄悄收回私密
        if (dto.getVisibility() != null && !dto.getVisibility().trim().isEmpty()) {
            album.setVisibility(normalizeVisibility(dto.getVisibility()));
        }
        albumMapper.update(album);
        return album;
    }

    @Override
    public void deleteAlbum(String id, String userId) {
        Album album = albumMapper.selectById(id);
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此相册");
        }
        // 软删除相册
        albumMapper.softDeleteById(id);
        // 软删除相册下的所有照片
        photoMapper.softDeleteByAlbumId(id);
    }

    @Override
    public List<Album> listDeletedAlbums(String userId) {
        return albumMapper.selectDeletedByUserId(userId);
    }

    @Override
    public void restoreAlbum(String id, String userId) {
        Album album = albumMapper.selectByIdIncludeDeleted(id);
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此相册");
        }
        if (!album.getIsDeleted()) {
            throw new RuntimeException("相册未被删除");
        }
        // 恢复相册
        albumMapper.restoreById(id);
        // 恢复相册下的所有照片
        photoMapper.restoreByAlbumId(id);
    }

    @Override
    public void permanentlyDeleteAlbum(String id, String userId) {
        Album album = albumMapper.selectByIdIncludeDeleted(id);
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (!album.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此相册");
        }
        // 删除相册下的所有照片文件
        List<com.photoshare.backend.entity.Photo> photos = photoMapper.selectByAlbumIdIncludeDeleted(id);
        for (com.photoshare.backend.entity.Photo photo : photos) {
            // 待审核区的本地临时文件与正式存储的文件分别处理
            if (pendingStorageService.isPendingUrl(photo.getFileUrl())) {
                pendingStorageService.delete(photo.getFileUrl());
            } else {
                storageService.delete(photo.getFileUrl());
            }
            photoMapper.deleteById(photo.getId());
        }
        // 删除相册
        albumMapper.deleteById(id);
    }

    private String normalizeVisibility(String visibility) {
        return visibility != null && "PUBLIC".equalsIgnoreCase(visibility.trim()) ? "PUBLIC" : "PRIVATE";
    }
}
