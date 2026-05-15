package com.ranadj.repository;

import com.ranadj.entity.DjPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing DjPackage database operations.
 */
@Repository
public interface PackageRepository extends JpaRepository<DjPackage, Long> {

    /**
     * Finds all packages with pagination filtered by active status.
     */
    Page<DjPackage> findByIsActive(boolean isActive, Pageable pageable);

    /**
     * Finds packages belonging to a specific admin.
     */
    Page<DjPackage> findByAdminId(Long adminId, Pageable pageable);
}
