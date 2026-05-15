package com.ranadj.repository;

import com.ranadj.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Payment database operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment record by its Razorpay Order ID.
     */
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);

    /**
     * Retrieves the list of payments associated with bookings belonging to a specific user.
     */
    List<Payment> findByBookingUserId(Long userId);

    /**
     * Retrieves payments for bookings whose package belongs to a specific admin (DJ Owner).
     * Ensures each admin only sees transactions for their own business.
     */
    List<Payment> findByBookingDjPackageAdminId(Long adminId);

    /**
     * Finds PENDING payments older than the specified cutoff time (for cleanup).
     */
    @org.springframework.data.jpa.repository.Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND p.createdAt < :cutoff")
    List<Payment> findStalePendingPayments(@org.springframework.data.repository.query.Param("cutoff") java.time.LocalDateTime cutoff);
}
