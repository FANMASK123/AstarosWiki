package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.message.MessageDTO;
import com.example.astaroswiki.entity.Message;
import com.example.astaroswiki.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final int MESSAGE_RETENTION_DAYS = 30;

    private final MessageRepository messageRepository;

    private LocalDateTime getRetentionDate() {
        return LocalDateTime.now().minusDays(MESSAGE_RETENTION_DAYS);
    }

    public Page<MessageDTO> getMessages(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository
            .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, getRetentionDate(), pageable
            );
        return messages.map(this::convertToDTO);
    }

    public long getUnreadCount(Long userId) {
        return messageRepository.countByUserIdAndIsReadFalseAndCreatedAtAfter(
            userId, getRetentionDate()
        );
    }

    public List<MessageDTO> getUnreadMessages(Long userId) {
        List<Message> messages = messageRepository
            .findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, getRetentionDate()
            );
        return messages.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void sendMessage(Long userId, String title, String content, Long pageId) {
        sendMessage(userId, title, content, null, pageId, null);
    }

    @Transactional
    public void sendMessage(Long userId, String title, String content, String opinion, Long pageId) {
        sendMessage(userId, title, content, opinion, pageId, null);
    }

    @Transactional
    public void sendMessage(Long userId, String title, String content, String opinion, Long pageId, String pageSlug) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setOpinion(opinion);
        message.setPageId(pageId);
        message.setPageSlug(pageSlug);
        message.setIsRead(false);
        messageRepository.save(message);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        Message message = messageRepository.findById(messageId)
            .orElseThrow(() -> new com.example.astaroswiki.exception.BusinessException("消息不存在"));
        message.setIsRead(true);
        messageRepository.save(message);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Message> messages = messageRepository
            .findByUserIdAndIsReadFalseAndCreatedAtAfterOrderByCreatedAtDesc(
                userId, getRetentionDate()
            );
        messages.forEach(m -> m.setIsRead(true));
        messageRepository.saveAll(messages);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.deleteById(messageId);
    }

    @Transactional
    public void deleteOldMessages(Long userId) {
        messageRepository.deleteByUserIdAndCreatedAtBefore(userId, getRetentionDate());
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldMessages() {
        messageRepository.findAll().forEach(m -> {
            if (m.getCreatedAt().isBefore(getRetentionDate())) {
                messageRepository.delete(m);
            }
        });
    }

    private MessageDTO convertToDTO(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setTitle(message.getTitle());
        dto.setContent(message.getContent());
        dto.setOpinion(message.getOpinion());
        dto.setIsRead(message.getIsRead());
        dto.setPageId(message.getPageId());
        dto.setPageSlug(message.getPageSlug());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
