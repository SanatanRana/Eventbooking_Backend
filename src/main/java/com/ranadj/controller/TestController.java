package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller to test routing security and Role-Based Access Control (RBAC).
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * GET /api/test/public : Access public content.
     */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<String>> publicEndpoint() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Public content accessed successfully")
                .data("This is public content.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/test/customer : Access customer-restricted content.
     */
    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> customerEndpoint() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Customer content accessed successfully")
                .data("This is content only accessible by registered customers/users.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/test/admin : Access admin-restricted content.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> adminEndpoint() {
        ApiResponse<String> response = ApiResponse.<String>builder()
                .success(true)
                .message("Admin content accessed successfully")
                .data("This is content only accessible by administrators.")
                .build();
        return ResponseEntity.ok(response);
    }
}
