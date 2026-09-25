package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.LikeDTO;
import com.photoshare.backend.dto.LikeSummaryDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@Tag(name = "点赞")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping
    @Operation(summary = "点赞相册或照片（重复点赞不报错）")
    public Result<Void> like(@Valid @RequestBody LikeDTO dto) {
        likeService.like(dto.getTargetType(), dto.getTargetId(), SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "取消点赞")
    public Result<Void> unlike(
            @RequestParam String targetType,
            @RequestParam String targetId) {
        likeService.unlike(targetType, targetId, SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/mine")
    @Operation(summary = "我点赞过的相册ID和照片ID")
    public Result<LikeSummaryDTO> myLikes() {
        return Result.success(likeService.getMyLikes(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/my-albums")
    @Operation(summary = "我点赞过的相册列表")
    public Result<List<Album>> myLikedAlbums() {
        return Result.success(likeService.listLikedAlbums(SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/my-photos")
    @Operation(summary = "我点赞过的照片列表")
    public Result<List<Photo>> myLikedPhotos() {
        return Result.success(likeService.listLikedPhotos(SecurityUtils.getCurrentUserId()));
    }
}
