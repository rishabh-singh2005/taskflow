package com.rrs.taskflow.controllers;

import com.rrs.taskflow.dtos.AuthResponseDto;
import com.rrs.taskflow.dtos.LoginRequestDto;
import com.rrs.taskflow.dtos.RefreshTokenRequestDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.response.ApiResponse;
import com.rrs.taskflow.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // register new user with optional profile image
    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<AuthResponseDto>> register(
            @Valid @RequestPart("data") RegisterRequestDto dto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) throws Exception {

        AuthResponseDto res = authService.register(dto, profileImage);

        ApiResponse<AuthResponseDto> response = new ApiResponse<>("User registered successfully", res);

        return ResponseEntity.ok(response);
    }

    // login with email and password — returns jwt and refresh token
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto
    ) {

        AuthResponseDto res = authService.login(dto);

        ApiResponse<AuthResponseDto> response = new ApiResponse<>("Login successful", res);

        return ResponseEntity.ok(response);
    }

    // send refresh token — get new access token without login
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto dto
    ) {

        AuthResponseDto res = authService.refreshToken(dto);

        ApiResponse<AuthResponseDto> response = new ApiResponse<>("Token refreshed successfully", res);

        return ResponseEntity.ok(response);
    }

    // logout  deletes refresh token of logged in user from db
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(Principal principal) {

        authService.logout(principal.getName());

        ApiResponse<String> response = new ApiResponse<>("Logout successful", null);

        return ResponseEntity.ok(response);
    }
}