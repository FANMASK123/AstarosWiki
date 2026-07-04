package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    long countByUserIdAndIsReadFalse(Long userId);
    
    List<Message> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    Page<Message> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        Long userId, LocalDateTime dateTime, Pageable pageable);

    List<Message> findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime dateTime);

    long countByUserIdAndIsReadFalseAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.userId = :userId AND m.createdAt < :dateTime")
    void deleteByUserIdAndCreatedAtBefore(@Param("userId") Long userId, @Param("dateTime") LocalDateTime dateTime);
}
