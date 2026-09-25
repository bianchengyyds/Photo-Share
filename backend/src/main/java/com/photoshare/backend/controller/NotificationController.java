package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.entity.Notification;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "站内通知")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    @Operation(summary = "通知列表，unreadOnly=true 只看未读")
    public Result<List<Notification>> list(
            @RequestParam(defaultValue = "false") boolean unreadOnly,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(notificationService.list(
                SecurityUtils.getCurrentUserId(), unreadOnly, page, size));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "未读通知数，用于导航栏角标")
    public Result<Integer> unreadCount() {
        return Result.success(notificationService.unreadCount(SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "标记单条通知已读")
    public Result<Void> markRead(@PathVariable String id) {
        notificationService.markRead(id, SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @PostMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除一条通知")
    public Result<Void> remove(@PathVariable String id) {
        notificationService.remove(id, SecurityUtils.getCurrentUserId());
        return Result.success();
    }
}
