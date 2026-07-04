package com.example.astaroswiki.repository;

import com.example.astaroswiki.entity.UserRequest;
import com.example.astaroswiki.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRequestRepository extends JpaRepository<UserRequest, Long> {
    List<UserRequest> findByUserId(Long userId);
    Page<UserRequest> findByStatus(RequestStatus status, Pageable pageable);
    Optional<UserRequest> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
