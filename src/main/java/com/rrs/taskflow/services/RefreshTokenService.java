package com.rrs.taskflow.services;

import com.rrs.taskflow.entities.RefreshToken;
import com.rrs.taskflow.entities.UserEntity;
import com.rrs.taskflow.repositories.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    // refresh token expiry in minutes — set in application.properties
    @Value("${jwt.refresh-expiration}")
    private long refreshTokenDurationMinutes;

    // creates new refresh token for user and saves in db
    public RefreshToken createRefreshToken(UserEntity user) {

        // if user already has a token, delete it first
        // so only one active token per user at a time
        refreshTokenRepository.findByUser(user)
                .ifPresent(existing -> refreshTokenRepository.delete(existing));

        RefreshToken refreshToken = new RefreshToken();

        // random unique string — ex: f3a8c9c2-91b2-4d3a-8f5e-7c1d2e9a6b10
        refreshToken.setToken(UUID.randomUUID().toString());

        // current time + expiry minutes from properties
        refreshToken.setExpiryDate(
                LocalDateTime.now().plusMinutes(refreshTokenDurationMinutes)
        );

        // link token to user
        refreshToken.setUser(user);

        return refreshTokenRepository.save(refreshToken);
    }

    // find token from db — used when client sends refresh token in api
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    // check if token is expired — if yes delete it and throw error
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            // token expired — remove from db
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired. Please login again.");
        }

        // token still valid
        return token;
    }

    // delete token on logout
    public void deleteByUser(UserEntity user) {
        refreshTokenRepository.findByUser(user)
                .ifPresent(token -> refreshTokenRepository.delete(token));
    }
}