package com.ranadj.service;

import com.ranadj.entity.SlotLock;
import com.ranadj.entity.User;
import com.ranadj.entity.Role;
import com.ranadj.exception.ApiException;
import com.ranadj.repository.SlotLockRepository;
import com.ranadj.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
public class SlotLockService {

    private final SlotLockRepository slotLockRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public SlotLockService(SlotLockRepository slotLockRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.slotLockRepository = slotLockRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public void acquireLock(Long adminId, LocalDate eventDate, LocalTime startTime, LocalTime endTime, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) {
            return; // Admins don't need to lock slots for themselves
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ApiException("Admin not found with id: " + adminId, HttpStatus.NOT_FOUND));

        if (admin.getRole() != Role.ADMIN) {
            throw new ApiException("User " + adminId + " is not a DJ Owner (Admin)", HttpStatus.BAD_REQUEST);
        }

        LocalDateTime startDateTime = LocalDateTime.of(eventDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(eventDate, endTime);
        if (endTime.isBefore(startTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        // Check if already locked by someone else
        boolean isLocked = slotLockRepository.existsOverlappingActiveLock(
                adminId, startDateTime, endDateTime, currentUser.getId());

        if (isLocked) {
            throw new ApiException("Slot is currently being booked by another user. Please try again in a few minutes.", HttpStatus.CONFLICT);
        }

        // Clean up any existing locks by this user for this admin/date first
        slotLockRepository.deleteByUserAndAdminAndEventDate(currentUser.getId(), adminId, eventDate);

        SlotLock lock = SlotLock.builder()
                .user(currentUser)
                .admin(admin)
                .eventDate(eventDate)
                .startTime(startTime)
                .endTime(endTime)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        slotLockRepository.save(lock);

        // Broadcast lock event
        messagingTemplate.convertAndSend("/topic/availability/" + adminId, 
                String.format("LOCKED:%s:%s:%s", eventDate, startTime, endTime));
    }

    public void releaseLock(Long adminId, LocalDate eventDate, User currentUser) {
        slotLockRepository.deleteByUserAndAdminAndEventDate(currentUser.getId(), adminId, eventDate);
        
        // Broadcast unlock event
        messagingTemplate.convertAndSend("/topic/availability/" + adminId, 
                String.format("UNLOCKED:%s", eventDate));
    }

    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupExpiredLocks() {
        slotLockRepository.deleteExpiredLocks(LocalDateTime.now());
        // Note: For a highly scalable system, we'd query which locks expired and broadcast UNLOCKED for each.
        // For simplicity, we just delete them. The frontend timer will expire anyway.
    }
}
