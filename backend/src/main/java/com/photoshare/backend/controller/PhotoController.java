package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.ContentAccessService;
import com.photoshare.backend.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "照片管理")
public class PhotoController {

    @Autowired
    private PhotoService photoService;

    @Autowired
    private ContentAccessService contentAccessService;

    @PostMapping("/albums/{albumId}/photos")
    @Operation(summary = "上传照片（支持多文件）")
    public Result<List<Photo>> uploadPhotos(
            @PathVariable String albumId,
            @RequestParam("files") List<MultipartFile> files) {
        String userId = SecurityUtils.getCurrentUserId();
        String userRole = SecurityUtils.getCurrentUserRole();
        List<Photo> photos = photoService.uploadPhotos(albumId, files, userId, userRole);
        return Result.success(photos);
    }

    @GetMapping("/albums/{albumId}/photos")
    @Operation(summary = "获取相册下照片列表")
    public Result<List<Photo>> listPhotos(@PathVariable String albumId) {
        String userId = SecurityUtils.getCurrentUserId();
        String userRole = SecurityUtils.getCurrentUserRole();
        // 先卡可见性，避免凭相册ID直读他人私密相册
        contentAccessService.requireViewable(albumId, userId, userRole);
        List<Photo> photos = photoService.listPhotos(albumId, userId, userRole);
        return Result.success(photos);
    }

    @DeleteMapping("/photos/{id}")
    @Operation(summary = "删除照片（软删除，移入回收站）")
    public Result<Void> deletePhoto(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        photoService.deletePhoto(id, userId);
        return Result.success();
    }

    @GetMapping("/trash/photos")
    @Operation(summary = "获取回收站中的照片列表")
    public Result<List<Photo>> listDeletedPhotos() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Photo> photos = photoService.listDeletedPhotos(userId);
        return Result.success(photos);
    }

    @PostMapping("/trash/photos/{id}/restore")
    @Operation(summary = "恢复回收站中的照片")
    public Result<Void> restorePhoto(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        photoService.restorePhoto(id, userId);
        return Result.success();
    }

    @DeleteMapping("/trash/photos/{id}")
    @Operation(summary = "永久删除回收站中的照片（不可恢复）")
    public Result<Void> permanentlyDeletePhoto(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        photoService.permanentlyDeletePhoto(id, userId);
        return Result.success();
    }

    @GetMapping("/albums/{albumId}/photos/trash")
    @Operation(summary = "获取相册中已删除的照片列表")
    public Result<List<Photo>> listDeletedPhotosByAlbumId(@PathVariable String albumId) {
        List<Photo> photos = photoService.listDeletedPhotosByAlbumId(albumId);
        return Result.success(photos);
    }
}
