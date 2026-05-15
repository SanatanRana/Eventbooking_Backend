package com.ranadj.service.impl;

import com.ranadj.dto.BookingRequest;
import com.ranadj.dto.BookingResponse;
import com.ranadj.entity.*;
import com.ranadj.exception.ApiException;
import com.ranadj.mapper.BookingMapper;
import com.ranadj.repository.BookingRepository;
import com.ranadj.repository.PackageRepository;
import com.ranadj.repository.UserRepository;
import com.ranadj.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Service implementation for managing Bookings.
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              BookingMapper bookingMapper,
                              PackageRepository packageRepository,
                              UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request, User currentUser) {
        // 1. Time & Date validations
        if (request.getStartTime().equals(request.getEndTime())) {
            throw new ApiException("End time cannot be exactly the same as start time", HttpStatus.BAD_REQUEST);
        }
        if (request.getEventDate().isBefore(java.time.LocalDate.now())) {
            throw new ApiException("Event date must not be in the past", HttpStatus.BAD_REQUEST);
        }
        if (request.getEventDate().isEqual(java.time.LocalDate.now()) && request.getStartTime().isBefore(java.time.LocalTime.now())) {
            throw new ApiException("Event start time must not be in the past", HttpStatus.BAD_REQUEST);
        }

        // Calculate actual DateTimes to handle overnight events (where end time is early morning next day)
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(request.getEventDate(), request.getStartTime());
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.of(request.getEventDate(), request.getEndTime());
        if (request.getEndTime().isBefore(request.getStartTime())) {
            endDateTime = endDateTime.plusDays(1);
        }

        // 2. Fetch User
        User bookingUser = currentUser;
        if (currentUser.getRole() == Role.ADMIN && request.getUserId() != null) {
            bookingUser = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ApiException("User not found with id: " + request.getUserId(), HttpStatus.NOT_FOUND));
        }

        // 3. Fetch Package
        DjPackage djPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ApiException("Package not found with id: " + request.getPackageId(), HttpStatus.NOT_FOUND));

        if (!djPackage.isActive()) {
            throw new ApiException("Cannot create booking for an inactive package", HttpStatus.BAD_REQUEST);
        }

        // 4. Calculate Financials
        BigDecimal totalAmount = djPackage.getPrice();
        BigDecimal advanceAmount = request.getAdvanceAmount();
        if (advanceAmount.compareTo(totalAmount) > 0) {
            throw new ApiException("Advance amount cannot exceed the total package price of " + totalAmount, HttpStatus.BAD_REQUEST);
        }
        BigDecimal remainingAmount = totalAmount.subtract(advanceAmount);

        // 5. Calculate Payment Status
        PaymentStatus paymentStatus = PaymentStatus.PENDING;
        if (advanceAmount.compareTo(totalAmount) == 0) {
            paymentStatus = PaymentStatus.PAID;
        } else if (advanceAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = PaymentStatus.PARTIAL;
        }

        // 6. Map and build booking
        Booking booking = bookingMapper.toEntity(request);
        booking.setUser(bookingUser);
        booking.setDjPackage(djPackage);
        booking.setTotalAmount(totalAmount);
        booking.setRemainingAmount(remainingAmount);
        booking.setPaymentStatus(paymentStatus);
        booking.setStartDateTime(startDateTime);
        booking.setEndDateTime(endDateTime);

        // Allow Admin to pre-assign status
        if (currentUser.getRole() == Role.ADMIN && request.getBookingStatus() != null) {
            try {
                booking.setBookingStatus(BookingStatus.valueOf(request.getBookingStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ApiException("Invalid booking status: " + request.getBookingStatus(), HttpStatus.BAD_REQUEST);
            }
        } else {
            booking.setBookingStatus(BookingStatus.PENDING);
        }

        // 7. Check for Slot Locks
        Long adminId = djPackage.getAdmin().getId();
        boolean isLockedBySomeoneElse = com.ranadj.ApplicationContextProvider.getApplicationContext()
                .getBean(com.ranadj.repository.SlotLockRepository.class)
                .existsOverlappingActiveLock(adminId, startDateTime, endDateTime, bookingUser.getId());
        
        if (isLockedBySomeoneElse) {
            throw new ApiException("Slot is currently locked by another user. Please try again later.", HttpStatus.CONFLICT);
        }

        // 8. Check for confirmed slot conflicts (scoped to this Admin/Vendor)
        if (booking.getBookingStatus() != BookingStatus.CANCELLED && booking.getBookingStatus() != BookingStatus.REJECTED) {
            boolean hasOverlap = bookingRepository.existsOverlappingConfirmedBooking(
                    startDateTime,
                    endDateTime,
                    adminId,
                    null
            );
            if (hasOverlap) {
                throw new ApiException("Double-Booking Intercepted: This timeslot is already confirmed for another event.", HttpStatus.BAD_REQUEST);
            }
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Delete the slot lock if exists
        com.ranadj.ApplicationContextProvider.getApplicationContext()
                .getBean(com.ranadj.service.SlotLockService.class)
                .releaseLock(adminId, booking.getEventDate(), bookingUser);

        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public BookingResponse updateBooking(Long id, BookingRequest request, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found with id: " + id, HttpStatus.NOT_FOUND));

        // 1. Security check
        if (currentUser.getRole() != Role.ADMIN) {
            if (!booking.getUser().getId().equals(currentUser.getId())) {
                throw new ApiException("Access Denied: You are not authorized to update this booking", HttpStatus.FORBIDDEN);
            }
            if (booking.getBookingStatus() != BookingStatus.PENDING) {
                throw new ApiException("Access Denied: You can only modify bookings that are in PENDING status", HttpStatus.BAD_REQUEST);
            }
        }

        // 2. Time validations
        if (request.getStartTime().equals(request.getEndTime())) {
            throw new ApiException("End time cannot be exactly the same as start time", HttpStatus.BAD_REQUEST);
        }
        if (request.getEventDate().isBefore(java.time.LocalDate.now())) {
            throw new ApiException("Event date must not be in the past", HttpStatus.BAD_REQUEST);
        }
        if (request.getEventDate().isEqual(java.time.LocalDate.now()) && request.getStartTime().isBefore(java.time.LocalTime.now())) {
            throw new ApiException("Event start time must not be in the past", HttpStatus.BAD_REQUEST);
        }

        // Calculate actual DateTimes to handle overnight events
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(request.getEventDate(), request.getStartTime());
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.of(request.getEventDate(), request.getEndTime());
        if (request.getEndTime().isBefore(request.getStartTime())) {
            endDateTime = endDateTime.plusDays(1);
        }

        // 3. Fetch package and update financials if changed
        DjPackage djPackage = booking.getDjPackage();
        if (!booking.getDjPackage().getId().equals(request.getPackageId())) {
            djPackage = packageRepository.findById(request.getPackageId())
                    .orElseThrow(() -> new ApiException("Package not found with id: " + request.getPackageId(), HttpStatus.NOT_FOUND));
            if (!djPackage.isActive()) {
                throw new ApiException("Cannot update booking to an inactive package", HttpStatus.BAD_REQUEST);
            }
            booking.setDjPackage(djPackage);
        }

        BigDecimal totalAmount = djPackage.getPrice();
        BigDecimal advanceAmount = request.getAdvanceAmount();
        if (advanceAmount.compareTo(totalAmount) > 0) {
            throw new ApiException("Advance amount cannot exceed the total package price of " + totalAmount, HttpStatus.BAD_REQUEST);
        }
        BigDecimal remainingAmount = totalAmount.subtract(advanceAmount);

        PaymentStatus paymentStatus = PaymentStatus.PENDING;
        if (advanceAmount.compareTo(totalAmount) == 0) {
            paymentStatus = PaymentStatus.PAID;
        } else if (advanceAmount.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = PaymentStatus.PARTIAL;
        }

        // Update fields
        bookingMapper.updateEntity(booking, request);
        booking.setTotalAmount(totalAmount);
        booking.setRemainingAmount(remainingAmount);
        booking.setPaymentStatus(paymentStatus);
        booking.setStartDateTime(startDateTime);
        booking.setEndDateTime(endDateTime);

        // Admin-only updates
        if (currentUser.getRole() == Role.ADMIN) {
            if (request.getBookingStatus() != null) {
                try {
                    BookingStatus newStatus = BookingStatus.valueOf(request.getBookingStatus().toUpperCase());
                    if (newStatus == BookingStatus.COMPLETED) {
                        if (java.time.LocalDateTime.now().isBefore(endDateTime)) {
                            throw new ApiException("Cannot mark event as COMPLETED before its scheduled end time.", HttpStatus.BAD_REQUEST);
                        }
                    }
                    booking.setBookingStatus(newStatus);
                } catch (IllegalArgumentException e) {
                    throw new ApiException("Invalid booking status: " + request.getBookingStatus(), HttpStatus.BAD_REQUEST);
                }
            }
            if (request.getPaymentStatus() != null) {
                try {
                    booking.setPaymentStatus(PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new ApiException("Invalid payment status: " + request.getPaymentStatus(), HttpStatus.BAD_REQUEST);
                }
            }
        } else {
            // Customer is allowed to self-cancel by setting request fields if desired
            if (request.getBookingStatus() != null && "CANCELLED".equalsIgnoreCase(request.getBookingStatus())) {
                booking.setBookingStatus(BookingStatus.CANCELLED);
            }
        }

        // 4. Overlap checks
        if (booking.getBookingStatus() != BookingStatus.CANCELLED && booking.getBookingStatus() != BookingStatus.REJECTED) {
            boolean hasOverlap = bookingRepository.existsOverlappingConfirmedBooking(
                    startDateTime,
                    endDateTime,
                    booking.getDjPackage().getAdmin().getId(),
                    booking.getId()
            );
            if (hasOverlap) {
                throw new ApiException("Double-Booking Intercepted: This timeslot is already confirmed for another event.", HttpStatus.BAD_REQUEST);
            }
        }

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toResponse(savedBooking);
    }

    @Override
    public void cancelBooking(Long id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found with id: " + id, HttpStatus.NOT_FOUND));

        if (currentUser.getRole() != Role.ADMIN && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Access Denied: You are not authorized to cancel this booking", HttpStatus.FORBIDDEN);
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found with id: " + id, HttpStatus.NOT_FOUND));

        if (currentUser.getRole() != Role.ADMIN && !booking.getUser().getId().equals(currentUser.getId())) {
            throw new ApiException("Access Denied: You are not authorized to view this booking", HttpStatus.FORBIDDEN);
        }

        return bookingMapper.toResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable, User currentUser) {
        Page<Booking> bookingsPage;

        if (currentUser.getRole() == Role.ADMIN) {
            bookingsPage = bookingRepository.findByDjPackage_Admin_IdAndIsArchivedByAdminFalse(currentUser.getId(), pageable);
        } else {
            bookingsPage = bookingRepository.findByUserIdAndIsArchivedByUserFalse(currentUser.getId(), pageable);
        }

        return bookingsPage.map(bookingMapper::toResponse);
    }

    @Override
    public void deleteBooking(Long id, User currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found with id: " + id, HttpStatus.NOT_FOUND));
        
        if (currentUser.getRole() == Role.ADMIN) {
            booking.setArchivedByAdmin(true);
        } else {
            booking.setArchivedByUser(true);
        }
        bookingRepository.save(booking);
    }
}
