package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.PageResult;
import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.message.MessageDTO;
import com.example.astaroswiki.service.MessageService;
import com.example.astaroswiki.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "消息管理", description = "消息相关接口")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Operation(summary = "获取消息列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<PageResult<MessageDTO>> getMessages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        Page<MessageDTO> messages = messageService.getMessages(userId, page, size);
        return Result.success(PageResult.of(messages));
    }

    @Operation(summary = "获取未读消息数量")
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<Long> getUnreadCount() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    @Operation(summary = "获取未读消息")
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<List<MessageDTO>> getUnreadMessages() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        List<MessageDTO> messages = messageService.getUnreadMessages(userId);
        return Result.success(messages);
    }

    @Operation(summary = "标记消息为已读")
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<Void> markAsRead(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    @Operation(summary = "标记所有消息为已读")
    @PutMapping("/all/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<Void> markAllAsRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            return Result.error("未登录");
        }
        messageService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除消息")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CREATOR')")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return Result.success();
    }
}
