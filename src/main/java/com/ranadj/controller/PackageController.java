package com.ranadj.controller;

import com.ranadj.dto.ApiResponse;
import com.ranadj.dto.PackageRequest;
import com.ranadj.dto.PackageResponse;
import com.ranadj.service.PackageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Controller exposing Package Management REST API Endpoints.
 * Access is controlled using Role-Based Access Control (RBAC).
 */
@RestController
@RequestMapping("/api/packages")
@Validated
public class PackageController {

    private final PackageService packageService;

    public PackageController(PackageService packageService) {
        this.packageService = packageService;
    }

    /**
     * Creates a new package. Accessible only by ADMIN.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @Valid @RequestBody PackageRequest request,
            @AuthenticationPrincipal com.ranadj.entity.User currentUser) {
        PackageResponse created = packageService.createPackage(request, currentUser);
        ApiResponse<PackageResponse> response = ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Package created successfully")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Updates an existing package by ID. Accessible only by ADMIN.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageRequest request,
            @AuthenticationPrincipal com.ranadj.entity.User currentUser) {
        PackageResponse updated = packageService.updatePackage(id, request, currentUser);
        ApiResponse<PackageResponse> response = ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Package updated successfully")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a package by ID. Accessible only by ADMIN.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePackage(@PathVariable Long id) {
        packageService.deletePackage(id);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(true)
                .message("Package deleted successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a single package by ID. Accessible by all visitors.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackageById(@PathVariable Long id) {
        PackageResponse djPackage = packageService.getPackageById(id);
        ApiResponse<PackageResponse> response = ApiResponse.<PackageResponse>builder()
                .success(true)
                .message("Package retrieved successfully")
                .data(djPackage)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all packages with sorting and pagination support.
     * Accessible by any authenticated user (CUSTOMER or ADMIN).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PackageResponse>>> getAllPackages(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Boolean activeOnly,
            @AuthenticationPrincipal com.ranadj.entity.User currentUser) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir.toLowerCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<PackageResponse> packages = packageService.getAllPackages(pageable, activeOnly, currentUser);
        
        ApiResponse<Page<PackageResponse>> response = ApiResponse.<Page<PackageResponse>>builder()
                .success(true)
                .message("Packages retrieved successfully")
                .data(packages)
                .build();
        return ResponseEntity.ok(response);
    }
}
