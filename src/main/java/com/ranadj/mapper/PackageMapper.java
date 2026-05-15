package com.ranadj.mapper;

import com.ranadj.dto.PackageRequest;
import com.ranadj.dto.PackageResponse;
import com.ranadj.entity.DjPackage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper for translating between DjPackage entity and DTO classes.
 */
@Component
public class PackageMapper {

    /**
     * Converts a PackageRequest DTO to a DjPackage entity.
     */
    public DjPackage toEntity(PackageRequest request) {
        if (request == null) {
            return null;
        }

        return DjPackage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationHours(request.getDurationHours())
                .features(request.getFeatures() != null ? new ArrayList<>(request.getFeatures()) : new ArrayList<>())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }

    /**
     * Converts a DjPackage entity to a PackageResponse DTO.
     */
    public PackageResponse toResponse(DjPackage entity) {
        if (entity == null) {
            return null;
        }

        return PackageResponse.builder()
                .id(entity.getId())
                .adminId(entity.getAdmin() != null ? entity.getAdmin().getId() : null)
                .adminName(entity.getAdmin() != null ? entity.getAdmin().getFullName() : null)
                .name(entity.getName())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .durationHours(entity.getDurationHours())
                .features(entity.getFeatures() != null ? new ArrayList<>(entity.getFeatures()) : new ArrayList<>())
                .isActive(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing DjPackage entity with fields from a PackageRequest DTO.
     */
    public void updateEntity(DjPackage entity, PackageRequest request) {
        if (entity == null || request == null) {
            return;
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setPrice(request.getPrice());
        entity.setDurationHours(request.getDurationHours());
        
        if (request.getFeatures() != null) {
            entity.getFeatures().clear();
            entity.getFeatures().addAll(request.getFeatures());
        }
        
        if (request.getIsActive() != null) {
            entity.setActive(request.getIsActive());
        }
    }
}
