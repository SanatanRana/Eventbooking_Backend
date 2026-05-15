package com.ranadj.service;

import com.ranadj.dto.PackageRequest;
import com.ranadj.dto.PackageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface defining package management business logic.
 */
public interface PackageService {

    /**
     * Creates a new package.
     */
    PackageResponse createPackage(PackageRequest request, com.ranadj.entity.User currentUser);

    /**
     * Updates an existing package.
     */
    PackageResponse updatePackage(Long id, PackageRequest request, com.ranadj.entity.User currentUser);

    /**
     * Deletes (purges) a package by its ID.
     */
    void deletePackage(Long id);

    /**
     * Retrieves a package by its ID.
     */
    PackageResponse getPackageById(Long id);

    /**
     * Retrieves all packages with advanced pagination support.
     * Optionally filters to only active packages.
     * Customers see all active packages. Admins see only their own packages.
     */
    Page<PackageResponse> getAllPackages(Pageable pageable, Boolean activeOnly, com.ranadj.entity.User currentUser);
}
