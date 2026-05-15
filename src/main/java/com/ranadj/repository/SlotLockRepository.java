package com.ranadj.repository;

import com.ranadj.entity.SlotLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Repository
public interface SlotLockRepository extends JpaRepository<SlotLock, Long> {

    @Query("SELECT COUNT(s) > 0 FROM SlotLock s WHERE s.admin.id = :adminId " +
           "AND s.expiresAt > CURRENT_TIMESTAMP " +
           "AND s.startDateTime < :endDateTime AND s.endDateTime > :startDateTime " +
           "AND (:excludeUserId IS NULL OR s.user.id <> :excludeUserId)")
    boolean existsOverlappingActiveLock(
            @Param("adminId") Long adminId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            @Param("excludeUserId") Long excludeUserId);

    @Modifying
    @Query("DELETE FROM SlotLock s WHERE s.expiresAt <= :now")
    void deleteExpiredLocks(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM SlotLock s WHERE s.user.id = :userId AND s.admin.id = :adminId AND s.eventDate = :eventDate")
    void deleteByUserAndAdminAndEventDate(
            @Param("userId") Long userId,
            @Param("adminId") Long adminId,
            @Param("eventDate") LocalDate eventDate);
}
