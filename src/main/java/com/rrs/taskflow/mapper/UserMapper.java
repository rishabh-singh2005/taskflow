package com.rrs.taskflow.mapper;

import com.rrs.taskflow.dtos.RegisterRequestDto;
import com.rrs.taskflow.entities.UserEntity;

public class UserMapper {

    // ================= RegisterRequestDto → UserEntity =================
    public static UserEntity toEntity(RegisterRequestDto dto) {
        UserEntity user = new UserEntity();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword());
        user.setRole(dto.getRole());
        return user;
    }

    // ================= UserEntity → RegisterRequestDto =================
    public static RegisterRequestDto toDto(UserEntity user) {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        // Note: password not mapped back for security reasons
        return dto;
    }
}