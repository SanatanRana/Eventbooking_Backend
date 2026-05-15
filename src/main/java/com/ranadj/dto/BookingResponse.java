package com.ranadj.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Data Transfer Object representing detailed response content for a booking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private String userPhone;
    private Long packageId;
    private String packageName;
    private Long adminId;
    private String adminName;
    private String adminEmail;
    private String adminPhone;
    private String eventType;
    private LocalDate eventDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String eventLocation;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private BigDecimal advanceAmount;
    private BigDecimal remainingAmount;
    private String bookingStatus;
    private String paymentStatus;
    private String specialNotes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
