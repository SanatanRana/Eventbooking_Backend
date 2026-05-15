package com.ranadj.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Data Transfer Object for creating or updating a Booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @NotNull(message = "Package ID is required")
    private Long packageId;

    // Optional for ADMIN, CUSTOMER userId is extracted from JWT
    private Long userId;

    @NotBlank(message = "Event type is required")
    @Size(min = 3, max = 50, message = "Event type must be between 3 and 50 characters")
    private String eventType;

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Event location is required")
    @Size(min = 5, max = 255, message = "Event location must be between 5 and 255 characters")
    private String eventLocation;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    private Integer guestCount;

    @NotNull(message = "Advance payment amount is required")
    @DecimalMin(value = "0.0", message = "Advance payment amount cannot be negative")
    private BigDecimal advanceAmount;

    @Size(max = 1000, message = "Special notes cannot exceed 1000 characters")
    private String specialNotes;

    // Optional status fields to allow ADMIN updates
    private String bookingStatus;
    private String paymentStatus;
}
