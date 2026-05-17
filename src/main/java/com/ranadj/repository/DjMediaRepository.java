package com.ranadj.repository;

import com.ranadj.entity.DjMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DjMediaRepository extends JpaRepository<DjMedia, Long> {
    List<DjMedia> findByAdminId(Long adminId);
}
