package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import com.ranadj.dto.DjOwnerResponse;
import com.ranadj.entity.Role;
import com.ranadj.entity.User;
import com.ranadj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for public-facing DJ Owner listing.
 */
@RestController
@RequestMapping("/api/dj-owners")
public class DjOwnerController {

    @Autowired
    private UserRepository userRepository;

    /**
     * GET /api/dj-owners : Returns all Admin/DJ Owner profiles (public info only).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<DjOwnerResponse>>> getAllDjOwners() {
        List<User> admins = userRepository.findByRole(Role.ADMIN);

        List<DjOwnerResponse> djOwners = admins.stream()
                .map(admin -> DjOwnerResponse.builder()
                        .id(admin.getId())
                        .fullName(admin.getFullName())
                        .phone(admin.getPhone())
                        .build())
                .collect(Collectors.toList());

        ApiResponse<List<DjOwnerResponse>> response = ApiResponse.<List<DjOwnerResponse>>builder()
                .success(true)
                .message("DJ Owners fetched successfully")
                .data(djOwners)
                .build();

        return ResponseEntity.ok(response);
    }
}
