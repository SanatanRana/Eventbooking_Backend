package com.ranadj.service.impl;

import com.ranadj.dto.AvailabilityResponse;
import com.ranadj.exception.ApiException;
import com.ranadj.repository.BookingRepository;
import com.ranadj.service.AvailabilityService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Service implementation for managing availability checks.
 */
@Service
@Transactional(readOnly = true)
public class AvailabilityServiceImpl implements AvailabilityService {

    private final BookingRepository bookingRepository;

    public AvailabilityServiceImpl(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Override
    public AvailabilityResponse checkAvailability(LocalDate date, LocalTime startTime, LocalTime endTime, Long adminId) {
        // 1. Validate Date (must be today or in the future)
        if (date.isBefore(LocalDate.now())) {
            throw new ApiException("Event date must be today or in the future", HttpStatus.BAD_REQUEST);
        }

        // 2. Validate Time (End Time must be after Start Time)
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new ApiException("End time must be strictly after start time", HttpStatus.BAD_REQUEST);
        }

        // 3. Query Database with indexed overlap search
        boolean isOverlapping = bookingRepository.existsOverlappingConfirmedBooking(date, startTime, endTime, adminId, null);

        if (isOverlapping) {
            return AvailabilityResponse.builder()
                    .available(false)
                    .message("Slot is unavailable: Already booked and confirmed by another event")
                    .build();
        }

        return AvailabilityResponse.builder()
                .available(true)
                .message("Slot available")
                .build();
    }
}
