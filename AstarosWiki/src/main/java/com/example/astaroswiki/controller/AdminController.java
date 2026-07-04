// ================================================================
// AdminController - 管理员控制器
// ================================================================
// 【学习要点】
// 1. 职责：提供管理员专用的 API 接口
// 2. 功能：用户管理、申请审批、页面管理、日志查询
// 3. 特点：所有接口都需要管理员权限（@PreAuthorize）
// 4. 位置：三层架构的 Controller 层，处理 HTTP 请求
// ================================================================

package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.PageResult;
import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.page.PageDTO;
import com.example.astaroswiki.entity.OperationLog;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.entity.UserRequest;
import com.example.astaroswiki.service.AdminService;
import com.example.astaroswiki.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// --------------------------------------------------------
// 类级别注解
// --------------------------------------------------------
@Tag(name = "管理员功能", description = "管理员相关接口")  // Swagger 文档分组标签
@RestController              // RESTful 控制器，返回 JSON
@RequestMapping("/api/admin") // 所有方法的 URL 前缀：/api/admin
@RequiredArgsConstructor     // Lombok：自动生成构造器，用于注入依赖
@PreAuthorize("hasRole('ADMIN')") // Spring Security：整个类只允许管理员访问
public class AdminController {

    // 依赖注入：通过构造器注入 AdminService
    private final AdminService adminService;

    // ========================================================
    // 用户管理功能
    // ========================================================

    // --------------------------------------------------------
    // 获取用户列表（分页）
    // --------------------------------------------------------
    // 【学习要点】@RequestParam：接收 URL 查询参数
    // 示例：GET /api/admin/users?page=0&size=10
    // 返回：分页的用户列表
    @Operation(summary = "获取用户列表")
    @GetMapping("/users")
    public Result<PageResult<User>> getUsers(
        @RequestParam(defaultValue = "0") int page,   // 页码（默认第1页，从0开始）
        @RequestParam(defaultValue = "10") int size    // 每页数量（默认10条）
    ) {
        // 调用 Service 层查询用户
        Page<User> users = adminService.getUsers(page, size);
        // PageResult.of(users)：将 Spring 的 Page 转换为自定义的分页结果
        return Result.success(PageResult.of(users)); // 返回成功结果，有数据
    }

    // --------------------------------------------------------
    // 获取待审核的申请列表
    // --------------------------------------------------------
    // 【学习要点】用户申请（注册、角色变更）需要管理员审批
    // 示例：GET /api/admin/requests?page=0&size=10
    @Operation(summary = "获取待审核申请")
    @GetMapping("/requests")
    public Result<PageResult<UserRequest>> getPendingRequests(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserRequest> requests = adminService.getPendingRequests(page, size);
        return Result.success(PageResult.of(requests)); // 返回成功结果，有数据
    }

    // --------------------------------------------------------
    // 审批通过申请
    // --------------------------------------------------------
    // 【学习要点】
    // 1. @PathVariable：从 URL 路径获取参数（如 /requests/5 中的 5）
    // 2. @RequestParam(required = false)：可选参数（审批意见）
    // 3. SecurityUtil.getCurrentUserId()：从 JWT Token 获取当前用户 ID
    // 示例：POST /api/admin/requests/5/approve?comment=同意
    @Operation(summary = "审批通过申请")
    @PostMapping("/requests/{requestId}/approve")
    public Result<Void> approveRequest(
        @PathVariable Long requestId,              // 申请ID（路径参数）
        @RequestParam(required = false) String comment // 审批意见（可选）
    ) {
        // 从 Spring Security Context 获取当前登录用户的 ID（审批人）
        Long reviewerId = SecurityUtil.getCurrentUserId();
        // 调用 Service 执行审批逻辑
        adminService.approveRequest(requestId, reviewerId, comment);
        return Result.success();
    }

    // --------------------------------------------------------
    // 拒绝申请
    // --------------------------------------------------------
    // 示例：POST /api/admin/requests/5/reject?comment=材料不全
    @Operation(summary = "拒绝申请")
    @PostMapping("/requests/{requestId}/reject")
    public Result<Void> rejectRequest(
        @PathVariable Long requestId,
        @RequestParam(required = false) String comment
    ) {
        Long reviewerId = SecurityUtil.getCurrentUserId();
        adminService.rejectRequest(requestId, reviewerId, comment);
        return Result.success();
    }

    // --------------------------------------------------------
    // 禁用用户
    // --------------------------------------------------------
    // 【学习要点】POST 方法用于执行操作（不同于删除，禁用可逆）
    // 示例：POST /api/admin/users/5/disable
    @Operation(summary = "禁用用户")
    @PostMapping("/users/{userId}/disable")
    public Result<Void> disableUser(@PathVariable Long userId) {
        adminService.disableUser(userId);
        return Result.success();
    }

    // --------------------------------------------------------
    // 启用用户
    // --------------------------------------------------------
    // 示例：POST /api/admin/users/5/enable
    @Operation(summary = "启用用户")
    @PostMapping("/users/{userId}/enable")
    public Result<Void> enableUser(@PathVariable Long userId) {
        adminService.enableUser(userId);
        return Result.success(); // 返回成功结果，无数据
    }

    // --------------------------------------------------------
    // 删除用户
    // --------------------------------------------------------
    // 【学习要点】DELETE 方法用于删除操作
    // 示例：DELETE /api/admin/users/5
    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return Result.success();
    }

    // ========================================================
    // 页面管理功能（回收站）
    // ========================================================
    // 【学习要点】软删除机制：删除只是修改状态，不是物理删除
    // 管理员可以：查看回收站、恢复页面、永久删除、锁定/解锁

    // --------------------------------------------------------
    // 获取回收站页面
    // --------------------------------------------------------
    // 示例：GET /api/admin/pages/deleted?page=0&size=10
    @Operation(summary = "获取回收站页面")
    @GetMapping("/pages/deleted")
    public Result<PageResult<PageDTO>> getDeletedPages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<PageDTO> pages = adminService.getDeletedPages(page, size);
        return Result.success(PageResult.of(pages));
    }

    // --------------------------------------------------------
    // 恢复已删除的页面
    // --------------------------------------------------------
    // 【学习要点】POST 方法执行恢复操作（不是 GET，因为有副作用）
    // 示例：POST /api/admin/pages/5/restore
    @Operation(summary = "恢复页面")
    @PostMapping("/pages/{pageId}/restore")
    public Result<Void> restorePage(@PathVariable Long pageId) {
        adminService.restorePage(pageId);
        return Result.success();
    }

    // --------------------------------------------------------
    // 永久删除页面（不可恢复）
    // --------------------------------------------------------
    // 【学习要点】物理删除，谨慎使用！删除后数据无法找回
    // 示例：DELETE /api/admin/pages/5/permanent
    @Operation(summary = "永久删除页面")
    @DeleteMapping("/pages/{pageId}/permanent")
    public Result<Void> permanentlyDeletePage(@PathVariable Long pageId) {
        adminService.permanentlyDeletePage(pageId);
        return Result.success();
    }

    // --------------------------------------------------------
    // 锁定页面（禁止编辑）
    // --------------------------------------------------------
    // 【学习要点】锁定后只有管理员可以解锁和编辑
    // 示例：POST /api/admin/pages/5/lock
    @Operation(summary = "锁定页面")
    @PostMapping("/pages/{pageId}/lock")
    public Result<Void> lockPage(@PathVariable Long pageId) {
        adminService.lockPage(pageId);
        return Result.success();
    }

    // --------------------------------------------------------
    // 解锁页面
    // --------------------------------------------------------
    // 示例：POST /api/admin/pages/5/unlock
    @Operation(summary = "解锁页面")
    @PostMapping("/pages/{pageId}/unlock")
    public Result<Void> unlockPage(@PathVariable Long pageId) {
        adminService.unlockPage(pageId);
        return Result.success();
    }

    // ========================================================
    // 操作日志功能（审计）
    // ========================================================
    // 【学习要点】审计日志：记录所有管理操作，便于追溯和监控
    // 类似于：谁在什么时间做了什么操作

    // --------------------------------------------------------
    // 获取操作日志（分页）
    // --------------------------------------------------------
    // 示例：GET /api/admin/logs?page=0&size=10
    @Operation(summary = "获取操作日志")
    @GetMapping("/logs")
    public Result<PageResult<OperationLog>> getOperationLogs(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<OperationLog> logs = adminService.getOperationLogs(page, size);
        return Result.success(PageResult.of(logs));
    }
}
