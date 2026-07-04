package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.page.PageDTO;
import com.example.astaroswiki.entity.OperationLog;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.entity.UserRequest;
import com.example.astaroswiki.entity.WikiPage;
import com.example.astaroswiki.enums.PageStatus;
import com.example.astaroswiki.enums.RequestStatus;
import com.example.astaroswiki.enums.UserStatus;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.OperationLogRepository;
import com.example.astaroswiki.repository.PageRepository;
import com.example.astaroswiki.repository.PageVersionRepository;
import com.example.astaroswiki.repository.UserRepository;
import com.example.astaroswiki.repository.UserRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;
    private final PageRepository pageRepository;
    private final PageVersionRepository pageVersionRepository;
    private final OperationLogRepository operationLogRepository;

    public Page<User> getUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    public Page<UserRequest> getPendingRequests(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRequestRepository.findByStatus(RequestStatus.PENDING, pageable);
    }

    @Transactional
    public void approveRequest(Long requestId, Long reviewerId, String comment) {
        UserRequest request = userRequestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException("申请不存在"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("申请已处理");
        }

        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new BusinessException("用户不存在"));

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewerId(reviewerId);
        request.setReviewComment(comment);
        userRequestRepository.save(request);

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void rejectRequest(Long requestId, Long reviewerId, String comment) {
        UserRequest request = userRequestRepository.findById(requestId)
            .orElseThrow(() -> new BusinessException("申请不存在"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessException("申请已处理");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewerId(reviewerId);
        request.setReviewComment(comment);
        userRequestRepository.save(request);
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无法禁用管理员账户");
        }

        user.setStatus(UserStatus.DISABLED);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        if (user.getRole().name().equals("ADMIN")) {
            throw new BusinessException("无法删除管理员账户");
        }

        userRepository.delete(user);
    }

    public Page<PageDTO> getDeletedPages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<WikiPage> pages = pageRepository.findByStatus(PageStatus.DELETED, pageable);
        return pages.map(this::convertToDTO);
    }

    @Transactional
    public void restorePage(Long pageId) {
        WikiPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (page.getStatus() != PageStatus.DELETED) {
            throw new BusinessException("页面未被删除");
        }

        page.setStatus(PageStatus.SAVED);
        page.setRejectReason(null);
        pageRepository.save(page);
    }

    @Transactional
    public void permanentlyDeletePage(Long pageId) {
        WikiPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        if (page.getStatus() != PageStatus.DELETED) {
            throw new BusinessException("请先删除页面");
        }

        pageRepository.delete(page);
    }

    @Transactional
    public void lockPage(Long pageId) {
        WikiPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        page.setStatus(PageStatus.LOCKED);
        pageRepository.save(page);
    }

    @Transactional
    public void unlockPage(Long pageId) {
        WikiPage page = pageRepository.findById(pageId)
            .orElseThrow(() -> new BusinessException("页面不存在"));

        page.setStatus(PageStatus.PUBLISHED);
        pageRepository.save(page);
    }

    public Page<OperationLog> getOperationLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return operationLogRepository.findAll(pageable);
    }

    private PageDTO convertToDTO(WikiPage page) {
        PageDTO dto = new PageDTO();
        dto.setId(page.getId());
        dto.setTitle(page.getTitle());
        dto.setSlug(page.getSlug());
        dto.setStatus(page.getStatus().name());
        dto.setCreatedAt(page.getCreatedAt());
        dto.setUpdatedAt(page.getUpdatedAt());
        return dto;
    }
}
