package com.ranadj.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Transfer Object for creating or updating a DJ package.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageRequest {

    @NotBlank(message = "Package name is required")
    @Size(min = 3, max = 100, message = "Package name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Package description is required")
    @Size(min = 10, max = 1000, message = "Package description must be between 10 and 1000 characters")
    private String description;

    @NotNull(message = "Package price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "Package duration is required")
    @Min(value = 0, message = "Duration cannot be negative")
    private Double durationHours;

    @NotEmpty(message = "At least one package feature is required")
    private List<String> features;

    @Builder.Default
    private Boolean isActive = true;
}
