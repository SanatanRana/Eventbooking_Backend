package com.ranadj.dto;

import lombok.*;

/**
 * Data Transfer Object returned upon successful login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private UserResponse user;
}
