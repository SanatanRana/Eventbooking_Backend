package com.ranadj.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for package details responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageResponse {

    private Long id;
    private Long adminId;
    private String adminName;
    private String name;
    private String description;
    private BigDecimal price;
    private Double durationHours;
    private List<String> features;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
