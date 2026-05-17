package com.ranadj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for exposing limited DJ/Admin profile data to public users.
 * Does NOT expose email, password, or any sensitive fields.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DjOwnerResponse {
    private Long id;
    private String fullName;
    private String phone;
}
