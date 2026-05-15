package com.ranadj.service;

import com.ranadj.dto.AvailabilityResponse;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service interface for checking DJ availability.
 */
public interface AvailabilityService {

    /**
     * Checks if a booking slot is available (i.e. has no overlapping CONFIRMED bookings for that admin).
     */
    AvailabilityResponse checkAvailability(LocalDate date, LocalTime startTime, LocalTime endTime, Long adminId);
}
