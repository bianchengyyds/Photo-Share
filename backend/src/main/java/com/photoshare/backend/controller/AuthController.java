package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.LoginRequest;
import com.photoshare.backend.dto.LoginResponse;
import com.photoshare.backend.dto.RegisterRequest;
import com.photoshare.backend.entity.User;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return Result.success(user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getCurrentUser() {
        String userId = SecurityUtils.getCurrentUserId();
        User user = authService.getCurrentUser(userId);
        return Result.success(user);
    }
}
