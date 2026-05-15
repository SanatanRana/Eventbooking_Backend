package com.ranadj.dto;

import com.ranadj.entity.Role;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Data Transfer Object containing secure user information for clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
