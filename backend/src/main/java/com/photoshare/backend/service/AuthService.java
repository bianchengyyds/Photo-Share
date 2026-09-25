package com.photoshare.backend.service;

import com.photoshare.backend.dto.LoginRequest;
import com.photoshare.backend.dto.LoginResponse;
import com.photoshare.backend.dto.RegisterRequest;
import com.photoshare.backend.entity.User;

public interface AuthService {

    User register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    User getCurrentUser(String userId);
}
