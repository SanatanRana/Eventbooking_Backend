package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import com.ranadj.dto.AvailabilityResponse;
import com.ranadj.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * REST Controller exposing Availability Management API Endpoints.
 */
@RestController
@RequestMapping("/api/availability")
@Validated
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    /**
     * Checks if a specific date and time slot is available for booking.
     * Publicly accessible.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<AvailabilityResponse>> checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam Long adminId) {

        AvailabilityResponse status = availabilityService.checkAvailability(date, startTime, endTime, adminId);
        ApiResponse<AvailabilityResponse> response = ApiResponse.<AvailabilityResponse>builder()
                .success(true)
                .message("Availability checked successfully")
                .data(status)
                .build();
        return ResponseEntity.ok(response);
    }
}
