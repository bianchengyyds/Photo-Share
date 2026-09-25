package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.AlbumDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.AlbumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
@Tag(name = "相册管理")
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @PostMapping
    @Operation(summary = "创建相册")
    public Result<Album> createAlbum(@Valid @RequestBody AlbumDTO dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Album album = albumService.createAlbum(dto, userId);
        return Result.success(album);
    }

    @GetMapping
    @Operation(summary = "获取相册列表")
    public Result<List<Album>> listAlbums() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Album> albums = albumService.listAlbums(userId);
        return Result.success(albums);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取相册详情（本人、管理员，或已关注且相册公开）")
    public Result<Album> getAlbum(@PathVariable String id) {
        Album album = albumService.getAlbumById(
                id, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUserRole());
        return Result.success(album);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新相册")
    public Result<Album> updateAlbum(@PathVariable String id, @Valid @RequestBody AlbumDTO dto) {
        String userId = SecurityUtils.getCurrentUserId();
        Album album = albumService.updateAlbum(id, dto, userId);
        return Result.success(album);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除相册（软删除，移入回收站）")
    public Result<Void> deleteAlbum(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        albumService.deleteAlbum(id, userId);
        return Result.success();
    }

    @GetMapping("/trash/list")
    @Operation(summary = "获取回收站中的相册列表")
    public Result<List<Album>> listDeletedAlbums() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Album> albums = albumService.listDeletedAlbums(userId);
        return Result.success(albums);
    }

    @PostMapping("/trash/{id}/restore")
    @Operation(summary = "恢复回收站中的相册")
    public Result<Void> restoreAlbum(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        albumService.restoreAlbum(id, userId);
        return Result.success();
    }

    @DeleteMapping("/trash/{id}")
    @Operation(summary = "永久删除回收站中的相册（不可恢复）")
    public Result<Void> permanentlyDeleteAlbum(@PathVariable String id) {
        String userId = SecurityUtils.getCurrentUserId();
        albumService.permanentlyDeleteAlbum(id, userId);
        return Result.success();
    }
}
