package com.ranadj.mapper;

import com.ranadj.dto.PaymentResponse;
import com.ranadj.entity.Payment;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting Payment database entities into safe transfer DTO payloads.
 */
@Component
public class PaymentMapper {

    /**
     * Converts a Payment entity into a PaymentResponse DTO.
     */
    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder builder = PaymentResponse.builder()
                .id(payment.getId())
                .bookingId(payment.getBooking().getId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt());

        // Populate customer identity from the booking's user
        com.ranadj.entity.Booking booking = payment.getBooking();
        if (booking != null) {
            builder.eventType(booking.getEventType());
            builder.eventDate(booking.getEventDate());

            com.ranadj.entity.User user = booking.getUser();
            if (user != null) {
                builder.customerName(user.getFullName());
                builder.customerEmail(user.getEmail());
                builder.customerPhone(user.getPhone());
            }
        }

        return builder.build();
    }
}
