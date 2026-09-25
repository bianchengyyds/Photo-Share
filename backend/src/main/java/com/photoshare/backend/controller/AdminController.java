package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员接口")
public class AdminController {

    @Autowired
    private PhotoService photoService;

    @GetMapping("/photos/pending")
    @Operation(summary = "获取待审核照片列表")
    public Result<List<Photo>> listPendingPhotos() {
        List<Photo> photos = photoService.listPendingPhotos();
        return Result.success(photos);
    }

    @GetMapping("/photos")
    @Operation(summary = "获取所有照片列表（管理员视图）")
    public Result<List<Photo>> listAllPhotos(@RequestParam(required = false) String status) {
        List<Photo> photos = photoService.listAllPhotosForAdmin(status);
        return Result.success(photos);
    }

    @PostMapping("/photos/{id}/approve")
    @Operation(summary = "审核通过照片")
    public Result<Void> approvePhoto(@PathVariable String id) {
        String adminId = SecurityUtils.getCurrentUserId();
        photoService.approvePhoto(id, adminId);
        return Result.success();
    }

    @PostMapping("/photos/{id}/reject")
    @Operation(summary = "审核拒绝照片")
    public Result<Void> rejectPhoto(@PathVariable String id, @RequestBody(required = false) Map<String, String> body) {
        String adminId = SecurityUtils.getCurrentUserId();
        String reason = body != null ? body.get("reason") : null;
        photoService.rejectPhoto(id, adminId, reason);
        return Result.success();
    }

    @PostMapping("/photos/batch-approve")
    @Operation(summary = "批量审核通过")
    public Result<Void> batchApprovePhotos(@RequestBody Map<String, List<String>> body) {
        String adminId = SecurityUtils.getCurrentUserId();
        List<String> photoIds = body.get("photoIds");
        photoService.batchApprovePhotos(photoIds, adminId);
        return Result.success();
    }

    @PostMapping("/photos/batch-reject")
    @Operation(summary = "批量审核拒绝")
    public Result<Void> batchRejectPhotos(@RequestBody Map<String, Object> body) {
        String adminId = SecurityUtils.getCurrentUserId();
        List<String> photoIds = (List<String>) body.get("photoIds");
        String reason = (String) body.get("reason");
        photoService.batchRejectPhotos(photoIds, adminId, reason);
        return Result.success();
    }
}
