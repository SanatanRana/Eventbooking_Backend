package com.ranadj.dto;

import lombok.*;

/**
 * Data Transfer Object representing availability query response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityResponse {
    private boolean available;
    private String message;
}
