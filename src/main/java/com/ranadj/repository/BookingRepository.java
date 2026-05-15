package com.ranadj.repository;

import com.ranadj.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Repository interface for Managing Booking database operations.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds paginated bookings for a specific customer.
     */
    Page<Booking> findByUserIdAndIsArchivedByUserFalse(Long userId, Pageable pageable);

    /**
     * Finds paginated bookings for a specific admin's packages.
     */
    Page<Booking> findByDjPackage_Admin_IdAndIsArchivedByAdminFalse(Long adminId, Pageable pageable);

    /**
     * Checks if there exists an overlapping CONFIRMED booking for the same admin on the given time range.
     * Overlap occurs when S1 < E2 AND E1 > S2.
     * Allows excluding a specific booking ID (used for avoiding self-collision during updates).
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "b.djPackage.admin.id = :adminId " +
           "AND b.bookingStatus = com.ranadj.entity.BookingStatus.CONFIRMED " +
           "AND b.startDateTime < :endDateTime AND b.endDateTime > :startDateTime " +
           "AND (:excludeId IS NULL OR b.id <> :excludeId)")
    boolean existsOverlappingConfirmedBooking(
            @Param("startDateTime") java.time.LocalDateTime startDateTime,
            @Param("endDateTime") java.time.LocalDateTime endDateTime,
            @Param("adminId") Long adminId,
            @Param("excludeId") Long excludeId);
}
