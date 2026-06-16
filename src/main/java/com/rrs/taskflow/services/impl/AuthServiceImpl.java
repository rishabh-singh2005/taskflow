package com.rrs.taskflow.services.impl;

import com.rrs.taskflow.dtos.AuthResponseDto;
import com.rrs.taskflow.dtos.LoginRequestDto;
import com.rrs.taskflow.dtos.RefreshTokenRequestDto;
import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.entities.RefreshToken;
import com.rrs.taskflow.entities.UserEntity;
import com.rrs.taskflow.exceptions.EmailAlreadyExistsException;
import com.rrs.taskflow.exceptions.ResourceNotFoundException;
import com.rrs.taskflow.file.FileStorageService;
import com.rrs.taskflow.mapper.UserMapper;
import com.rrs.taskflow.repositories.UserRepository;
import com.rrs.taskflow.security.JwtUtil;
import com.rrs.taskflow.services.AuthService;
import com.rrs.taskflow.services.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private FileStorageService fileStorageService;

    // register new user — saves to db with encoded password and optional image
    @Override
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto, MultipartFile profileImage) throws Exception {

        // check if email already taken
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered: " + dto.getEmail());
        }

        // convert dto to entity using mapper
        UserEntity user = UserMapper.toEntity(dto);

        // encode plain password before saving
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        // save profile image if provided
        if (profileImage != null && !profileImage.isEmpty()) {
            String imagePath = fileStorageService.saveProfileImage(profileImage);
            user.setProfileImage(imagePath);
        }

        UserEntity savedUser = userRepository.save(user);

        // generate jwt access token
        String accessToken = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole().name()
        );

        // generate and save refresh token in db
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponseDto(
                accessToken,
                refreshToken.getToken(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    // login — validate credentials, return jwt + refresh token
    @Override
    @Transactional
    public AuthResponseDto login(LoginRequestDto dto) {

        // spring security validates email and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        // if we reach here — credentials are valid
        UserEntity user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // generate fresh jwt access token
        String accessToken = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // generate new refresh token — old one gets deleted automatically
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponseDto(
                accessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // refresh token — validate refresh token, issue new access token
    @Override
    @Transactional
    public AuthResponseDto refreshToken(RefreshTokenRequestDto dto) {

        // find refresh token in db
        RefreshToken refreshToken = refreshTokenService.findByToken(dto.getRefreshToken())
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found. Please login again."));

        // check if token is expired — throws error if expired
        refreshTokenService.verifyExpiration(refreshToken);

        // get user linked to this refresh token
        UserEntity user = refreshToken.getUser();

        // generate new access token
        String newAccessToken = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole().name()
        );

        // return new access token with same refresh token
        return new AuthResponseDto(
                newAccessToken,
                refreshToken.getToken(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // logout — delete refresh token from db
    @Override
    @Transactional
    public void logout(String email) {

        // find user by email
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // delete their refresh token
        refreshTokenService.deleteByUser(user);
    }
}