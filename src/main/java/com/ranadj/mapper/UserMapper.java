package com.ranadj.mapper;

import com.ranadj.dto.UserResponse;
import com.ranadj.entity.User;
import org.springframework.stereotype.Component;

/**
 * Mapper component for mapping Entity objects to DTOs.
 */
@Component
public class UserMapper {

    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user User entity
     * @return UserResponse DTO
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .isVerified(user.isVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
