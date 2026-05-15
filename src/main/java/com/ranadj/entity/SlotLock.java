package com.ranadj.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a temporary slot lock for booking.
 */
@Entity
@Table(name = "slot_locks", indexes = {
    @Index(name = "idx_slotlock_admin_date", columnList = "admin_id, event_date"),
    @Index(name = "idx_slotlock_datetime", columnList = "start_date_time, end_date_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlotLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Customer who holds the lock

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin; // The DJ Owner whose slot is locked

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    // Internal fields for logical processing and overlapping checks
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
