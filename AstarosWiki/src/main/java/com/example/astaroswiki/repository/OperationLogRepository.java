package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.OperationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    Page<OperationLog> findByUserId(Long userId, Pageable pageable);
    Page<OperationLog> findByAction(String action, Pageable pageable);
    Page<OperationLog> findByTargetTypeAndTargetId(String targetType, Long targetId, Pageable pageable);
    Page<OperationLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
}
