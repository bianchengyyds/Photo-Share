package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.AlbumCommentDTO;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.AlbumCommentService;
import com.photoshare.backend.service.ContentAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "相册评论")
public class AlbumCommentController {

    @Autowired
    private AlbumCommentService albumCommentService;

    @Autowired
    private ContentAccessService contentAccessService;

    @GetMapping("/albums/{albumId}/comments")
    @Operation(summary = "获取相册评论列表（含一级回复）")
    public Result<List<AlbumComment>> listComments(@PathVariable String albumId) {
        contentAccessService.requireViewable(
                albumId, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUserRole());
        return Result.success(albumCommentService.listComments(albumId));
    }

    @PostMapping("/albums/{albumId}/comments")
    @Operation(summary = "发表评论或回复（只有相册可以被评论）")
    public Result<AlbumComment> addComment(
            @PathVariable String albumId,
            @Valid @RequestBody AlbumCommentDTO dto) {
        contentAccessService.requireViewable(
                albumId, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUserRole());
        AlbumComment comment = albumCommentService.addComment(
                albumId, dto, SecurityUtils.getCurrentUserId());
        return Result.success(comment);
    }

    @DeleteMapping("/comments/{id}")
    @Operation(summary = "删除评论（评论作者或相册所有者）")
    public Result<Void> deleteComment(@PathVariable String id) {
        albumCommentService.deleteComment(id, SecurityUtils.getCurrentUserId());
        return Result.success();
    }
}
