package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.service.PhotoService;
import com.photoshare.backend.service.TagService;
import com.photoshare.backend.storage.PendingStorageService;
import com.photoshare.backend.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private StorageService storageService;

    @Autowired
    private PendingStorageService pendingStorageService;

    @Autowired
    private TagService tagService;

    @Override
    public List<Photo> uploadPhotos(String albumId, List<MultipartFile> files, String userId, String userRole) {
        List<Photo> photos = new ArrayList<>();
        boolean isAdmin = "ADMIN".equals(userRole);
        for (MultipartFile file : files) {
            // 普通用户：先存入待审核区（本地临时目录），审核通过后才转存到正式存储（七牛云）
            // 管理员：直接存入正式存储，无需审核
            String fileUrl = isAdmin ? storageService.upload(file) : pendingStorageService.save(file);
            Photo photo = new Photo();
            photo.setId(UUID.randomUUID().toString());
            photo.setAlbumId(albumId);
            photo.setFileName(file.getOriginalFilename());
            photo.setFileUrl(fileUrl);
            photo.setFileSize(file.getSize());
            photo.setMimeType(file.getContentType());
            photo.setSortOrder(0);
            photo.setUserId(userId);
            photo.setStatus(isAdmin ? "approved" : "pending");
            photoMapper.insert(photo);
            photos.add(photo);
        }
        return photos;
    }

    @Override
    public List<Photo> uploadContributorPhotos(String albumId, List<MultipartFile> files, String ownerId, String uploaderNickname) {
        List<Photo> photos = new ArrayList<>();
        for (MultipartFile file : files) {
            // 协作者上传一律先进入待审核区（本地临时目录），管理员审核通过后才转存到正式存储
            Photo photo = new Photo();
            photo.setId(UUID.randomUUID().toString());
            photo.setAlbumId(albumId);
            photo.setFileName(file.getOriginalFilename());
            photo.setFileUrl(pendingStorageService.save(file));
            photo.setFileSize(file.getSize());
            photo.setMimeType(file.getContentType());
            photo.setSortOrder(0);
            photo.setUserId(ownerId);
            photo.setStatus("pending");
            photo.setUploaderNickname(uploaderNickname);
            photoMapper.insert(photo);
            photos.add(photo);
        }
        return photos;
    }

    @Override
    public List<Photo> listPhotos(String albumId, String userId, String userRole) {
        List<Photo> photos;
        // 管理员可以看到所有照片
        if ("ADMIN".equals(userRole)) {
            photos = photoMapper.selectByAlbumId(albumId);
        } else if (userId == null) {
            // 分享页访客（无登录用户）：只能看到已审核通过的照片
            photos = photoMapper.selectApprovedByAlbumId(albumId);
        } else {
            // 相册所有者：除已通过的照片外，自己上传的待审核/已拒绝照片也一并返回，用于查看审核进度
            photos = photoMapper.selectByAlbumIdForOwner(albumId, userId);
        }
        attachTags(photos, albumId);
        return photos;
    }

    private void attachTags(List<Photo> photos, String albumId) {
        Map<String, List<TagViewDTO>> tagMap = tagService.mapTagsForAlbum(albumId);
        for (Photo photo : photos) {
            photo.setTags(tagMap.getOrDefault(photo.getId(), new ArrayList<>()));
        }
    }

    @Override
    public void deletePhoto(String id, String userId) {
        Photo photo = photoMapper.selectById(id);
        if (photo != null) {
            if (!photo.getUserId().equals(userId)) {
                throw new RuntimeException("无权操作此照片");
            }
            // 软删除：只标记，不删除文件
            photoMapper.softDeleteById(id);
        }
    }

    @Override
    public List<Photo> listDeletedPhotos(String userId) {
        return photoMapper.selectDeletedByUserId(userId);
    }

    @Override
    public void restorePhoto(String id, String userId) {
        Photo photo = photoMapper.selectByIdIncludeDeleted(id);
        if (photo == null) {
            throw new RuntimeException("照片不存在");
        }
        if (!photo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此照片");
        }
        if (!photo.getIsDeleted()) {
            throw new RuntimeException("照片未被删除");
        }
        photoMapper.restoreById(id);
    }

    @Override
    public void permanentlyDeletePhoto(String id, String userId) {
        Photo photo = photoMapper.selectByIdIncludeDeleted(id);
        if (photo == null) {
            throw new RuntimeException("照片不存在");
        }
        if (!photo.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此照片");
        }
        // 删除文件：待审核区的本地临时文件与正式存储的文件分别处理
        if (pendingStorageService.isPendingUrl(photo.getFileUrl())) {
            pendingStorageService.delete(photo.getFileUrl());
        } else {
            storageService.delete(photo.getFileUrl());
        }
        // 物理删除记录
        photoMapper.deleteById(id);
    }

    @Override
    public List<Photo> listDeletedPhotosByAlbumId(String albumId) {
        return photoMapper.selectDeletedByAlbumId(albumId);
    }

    @Override
    public List<Photo> listPendingPhotos() {
        return photoMapper.selectPendingPhotos();
    }

    @Override
    public List<Photo> listAllPhotosForAdmin(String status) {
        if (status != null && !status.isEmpty()) {
            return photoMapper.selectPhotosByStatus(status);
        }
        return photoMapper.selectAllPhotosForAdmin();
    }

    @Override
    public void approvePhoto(String photoId, String adminId) {
        Photo photo = photoMapper.selectByIdIncludeDeleted(photoId);
        if (photo == null) {
            throw new RuntimeException("照片不存在");
        }
        // 待审核区的照片：审核通过时才转存到正式存储（七牛云），随后删除本地临时文件
        String pendingUrl = photo.getFileUrl();
        if (pendingStorageService.isPendingUrl(pendingUrl)) {
            byte[] data = pendingStorageService.readBytes(pendingUrl);
            photo.setFileUrl(storageService.upload(data, photo.getFileName()));
            pendingStorageService.delete(pendingUrl);
        }
        photo.setStatus("approved");
        photo.setReviewedBy(adminId);
        photo.setReviewedAt(LocalDateTime.now());
        photo.setRejectReason(null);
        photoMapper.updateReviewStatus(photo);
    }

    @Override
    public void rejectPhoto(String photoId, String adminId, String reason) {
        Photo photo = photoMapper.selectByIdIncludeDeleted(photoId);
        if (photo == null) {
            throw new RuntimeException("照片不存在");
        }
        photo.setStatus("rejected");
        photo.setReviewedBy(adminId);
        photo.setReviewedAt(LocalDateTime.now());
        photo.setRejectReason(reason);
        photoMapper.updateReviewStatus(photo);
    }

    @Override
    public void batchApprovePhotos(List<String> photoIds, String adminId) {
        for (String photoId : photoIds) {
            approvePhoto(photoId, adminId);
        }
    }

    @Override
    public void batchRejectPhotos(List<String> photoIds, String adminId, String reason) {
        for (String photoId : photoIds) {
            rejectPhoto(photoId, adminId, reason);
        }
    }
}
