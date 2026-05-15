package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import com.ranadj.entity.User;
import com.ranadj.service.SlotLockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
public class SlotLockController {

    private final SlotLockService slotLockService;

    public SlotLockController(SlotLockService slotLockService) {
        this.slotLockService = slotLockService;
    }

    @PostMapping("/lock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> lockSlot(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal User currentUser) {
        
        Long adminId = Long.valueOf(payload.get("adminId").toString());
        LocalDate eventDate = LocalDate.parse(payload.get("eventDate").toString());
        LocalTime startTime = LocalTime.parse(payload.get("startTime").toString());
        LocalTime endTime = LocalTime.parse(payload.get("endTime").toString());

        slotLockService.acquireLock(adminId, eventDate, startTime, endTime, currentUser);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Slot locked successfully")
                .build());
    }

    @PostMapping("/unlock")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unlockSlot(
            @RequestBody Map<String, Object> payload,
            @AuthenticationPrincipal User currentUser) {

        Long adminId = Long.valueOf(payload.get("adminId").toString());
        LocalDate eventDate = LocalDate.parse(payload.get("eventDate").toString());

        slotLockService.releaseLock(adminId, eventDate, currentUser);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Slot unlocked successfully")
                .build());
    }
}
