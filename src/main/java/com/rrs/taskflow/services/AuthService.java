package com.rrs.taskflow.services;

import com.rrs.taskflow.dtos.AuthResponseDto;
import com.rrs.taskflow.dtos.LoginRequestDto;
import com.rrs.taskflow.dtos.RefreshTokenRequestDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {

    // Register new user with optional profile image
    AuthResponseDto register(RegisterRequestDto dto, MultipartFile profileImage) throws Exception;

    // Login with email and password — returns JWT + refresh token
    AuthResponseDto login(LoginRequestDto dto);

    // Send refresh token — get new access token
    AuthResponseDto refreshToken(RefreshTokenRequestDto dto);

    // Logout — delete refresh token from DB
    void logout(String email);
}