package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import com.ranadj.dto.BookingRequest;
import com.ranadj.dto.BookingResponse;
import com.ranadj.entity.User;
import com.ranadj.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing Booking Module API Endpoints.
 */
@RestController
@RequestMapping("/api/bookings")
@Validated
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new booking. Accessible by any authenticated user.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User currentUser) {
        
        BookingResponse created = bookingService.createBooking(request, currentUser);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking created successfully")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing booking. Accessible by any authenticated user.
     * Non-admins can only update their own bookings that are in PENDING status.
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody BookingRequest request,
            @AuthenticationPrincipal User currentUser) {

        BookingResponse updated = bookingService.updateBooking(id, request, currentUser);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking updated successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Cancels an existing booking. Accessible by owner or admin.
     */
    @PutMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        bookingService.cancelBooking(id, currentUser);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Booking cancelled successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single booking by ID. Accessible by owner or admin.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {

        BookingResponse booking = bookingService.getBookingById(id, currentUser);
        ApiResponse<BookingResponse> response = ApiResponse.<BookingResponse>builder()
                .success(true)
                .message("Booking retrieved successfully")
                .data(booking)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all bookings with sorting and pagination support.
     * Customers only see their own bookings; Admins see all bookings across the platform.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal User currentUser) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<BookingResponse> bookings = bookingService.getAllBookings(pageable, currentUser);

        ApiResponse<Page<BookingResponse>> response = ApiResponse.<Page<BookingResponse>>builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .data(bookings)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a booking from the database (Soft Delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        bookingService.deleteBooking(id, currentUser);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Booking deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
