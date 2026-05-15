package com.ranadj.mapper;

import com.ranadj.dto.BookingRequest;
import com.ranadj.dto.BookingResponse;
import com.ranadj.entity.Booking;
import org.springframework.stereotype.Component;

/**
 * Mapper for translating between Booking entities and DTOs.
 */
@Component
public class BookingMapper {

    /**
     * Converts a BookingRequest DTO to a baseline Booking entity.
     * Note: Associations (User, DjPackage) and financial calculations are filled separately in the Service.
     */
    public Booking toEntity(BookingRequest request) {
        if (request == null) {
            return null;
        }

        return Booking.builder()
                .eventType(request.getEventType())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .eventLocation(request.getEventLocation())
                .guestCount(request.getGuestCount())
                .advanceAmount(request.getAdvanceAmount())
                .specialNotes(request.getSpecialNotes())
                .build();
    }

    /**
     * Converts a Booking entity to a complete BookingResponse DTO.
     */
    public BookingResponse toResponse(Booking entity) {
        if (entity == null) {
            return null;
        }

        BookingResponse.BookingResponseBuilder builder = BookingResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .eventDate(entity.getEventDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .eventLocation(entity.getEventLocation())
                .guestCount(entity.getGuestCount())
                .totalAmount(entity.getTotalAmount())
                .advanceAmount(entity.getAdvanceAmount())
                .remainingAmount(entity.getRemainingAmount())
                .bookingStatus(entity.getBookingStatus().name())
                .paymentStatus(entity.getPaymentStatus().name())
                .specialNotes(entity.getSpecialNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());

        if (entity.getUser() != null) {
            builder.userId(entity.getUser().getId())
                   .userFullName(entity.getUser().getFullName())
                   .userEmail(entity.getUser().getEmail())
                   .userPhone(entity.getUser().getPhone());
        }

        if (entity.getDjPackage() != null) {
            builder.packageId(entity.getDjPackage().getId())
                   .packageName(entity.getDjPackage().getName());
            if (entity.getDjPackage().getAdmin() != null) {
                builder.adminId(entity.getDjPackage().getAdmin().getId())
                       .adminName(entity.getDjPackage().getAdmin().getFullName())
                       .adminEmail(entity.getDjPackage().getAdmin().getEmail())
                       .adminPhone(entity.getDjPackage().getAdmin().getPhone());
            }
        }

        return builder.build();
    }

    /**
     * Updates an existing Booking entity with mutable fields from a BookingRequest.
     */
    public void updateEntity(Booking entity, BookingRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setEventType(request.getEventType());
        entity.setEventDate(request.getEventDate());
        entity.setStartTime(request.getStartTime());
        entity.setEndTime(request.getEndTime());
        entity.setEventLocation(request.getEventLocation());
        entity.setGuestCount(request.getGuestCount());
        entity.setAdvanceAmount(request.getAdvanceAmount());
        entity.setSpecialNotes(request.getSpecialNotes());
    }
}
