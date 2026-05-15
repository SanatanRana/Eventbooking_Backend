package com.ranadj.service;

import com.ranadj.dto.BookingRequest;
import com.ranadj.dto.BookingResponse;
import com.ranadj.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface specifying Booking operations.
 */
public interface BookingService {

    /**
     * Creates a new booking.
     */
    BookingResponse createBooking(BookingRequest request, User currentUser);

    /**
     * Updates an existing booking.
     */
    BookingResponse updateBooking(Long id, BookingRequest request, User currentUser);

    /**
     * Cancels a booking (marks status as CANCELLED).
     */
    void cancelBooking(Long id, User currentUser);

    /**
     * Retrieves a booking by its ID, with owner checks.
     */
    BookingResponse getBookingById(Long id, User currentUser);

    /**
     * Retrieves paginated bookings.
     * Customers will only see their own bookings; Admins will see all.
     */
    Page<BookingResponse> getAllBookings(Pageable pageable, User currentUser);

    /**
     * Deletes a booking from the database (soft delete based on role).
     */
    void deleteBooking(Long id, User currentUser);
}
