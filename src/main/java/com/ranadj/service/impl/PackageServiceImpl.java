package com.ranadj.service.impl;

import com.ranadj.dto.PackageRequest;
import com.ranadj.dto.PackageResponse;
import com.ranadj.entity.DjPackage;
import com.ranadj.exception.ApiException;
import com.ranadj.mapper.PackageMapper;
import com.ranadj.repository.PackageRepository;
import com.ranadj.service.PackageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing packages.
 */
@Service
@Transactional
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final PackageMapper packageMapper;

    public PackageServiceImpl(PackageRepository packageRepository, PackageMapper packageMapper) {
        this.packageRepository = packageRepository;
        this.packageMapper = packageMapper;
    }

    @Override
    public PackageResponse createPackage(PackageRequest request, com.ranadj.entity.User currentUser) {
        DjPackage packageEntity = packageMapper.toEntity(request);
        packageEntity.setAdmin(currentUser);
        DjPackage savedEntity = packageRepository.save(packageEntity);
        return packageMapper.toResponse(savedEntity);
    }

    @Override
    public PackageResponse updatePackage(Long id, PackageRequest request, com.ranadj.entity.User currentUser) {
        DjPackage packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Package not found with id: " + id, HttpStatus.NOT_FOUND));

        if (!packageEntity.getAdmin().getId().equals(currentUser.getId())) {
            throw new ApiException("Access Denied: You can only update your own packages", HttpStatus.FORBIDDEN);
        }

        packageMapper.updateEntity(packageEntity, request);
        DjPackage updatedEntity = packageRepository.save(packageEntity);
        return packageMapper.toResponse(updatedEntity);
    }

    @Override
    public void deletePackage(Long id) {
        if (!packageRepository.existsById(id)) {
            throw new ApiException("Package not found with id: " + id, HttpStatus.NOT_FOUND);
        }
        packageRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponse getPackageById(Long id) {
        DjPackage packageEntity = packageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Package not found with id: " + id, HttpStatus.NOT_FOUND));
        return packageMapper.toResponse(packageEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PackageResponse> getAllPackages(Pageable pageable, Boolean activeOnly, com.ranadj.entity.User currentUser) {
        Page<DjPackage> packagePage;
        
        if (currentUser != null && currentUser.getRole() == com.ranadj.entity.Role.ADMIN) {
            packagePage = packageRepository.findByAdminId(currentUser.getId(), pageable);
        } else {
            if (Boolean.TRUE.equals(activeOnly)) {
                packagePage = packageRepository.findByIsActive(true, pageable);
            } else {
                packagePage = packageRepository.findAll(pageable);
            }
        }

        return packagePage.map(packageMapper::toResponse);
    }
}
