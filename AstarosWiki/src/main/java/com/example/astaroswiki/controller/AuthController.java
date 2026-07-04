package com.example.astaroswiki.controller;

// ============================================================
// AuthController - 认证控制器（API 接口层）
// ============================================================
// 
// 【Spring MVC Controller 知识点】
// 1. @RestController: RESTful API 控制器
// 2. @RequestMapping: 定义路由前缀
// 3. @PostMapping: POST 请求映射
// 4. @RequestBody: 接收 JSON 请求体
// 5. @Valid: 请求参数验证
// 
// 【推荐阅读】
// https://www.baeldung.com/spring-controller
// ============================================================

import com.example.astaroswiki.common.Result;
import com.example.astaroswiki.dto.auth.AuthResponse;
import com.example.astaroswiki.dto.auth.LoginRequest;
import com.example.astaroswiki.dto.auth.RegisterRequest;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.repository.UserRepository;
import com.example.astaroswiki.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// --------------------------------------------------------
// Swagger 注解
// --------------------------------------------------------
// @Tag: API 文档分组标签
// name = "认证管理": API 文档中的分组名称
// description = "用户认证相关接口": 分组描述
// 【学习要点】Swagger 用于生成 API 文档
@Tag(name = "认证管理", description = "用户认证相关接口")

// --------------------------------------------------------
// @RestController 注解
// --------------------------------------------------------
// 标记这是一个 RESTful API 控制器
// 【学习要点】@RestController = @Controller + @ResponseBody
// 所有方法返回值自动转为 JSON
@RestController

// --------------------------------------------------------
// @RequestMapping 注解
// --------------------------------------------------------
// 定义路由前缀
// "/api/auth": 所有方法的 URL 都以 /api/auth 开头
// 例如：login() → /api/auth/login
// 【RESTful 设计】使用名词而非动词，如 auth 而非 authenticate
@RequestMapping("/api/auth")

// --------------------------------------------------------
// @RequiredArgsConstructor 注解
// --------------------------------------------------------
// Lombok 自动生成构造器注入
@RequiredArgsConstructor
public class AuthController {

    // ========================================================
    // 依赖注入
    // ========================================================
    // 【学习要点】Controller 依赖 Service 层处理业务逻辑
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    // ========================================================
    // 登录接口
    // ========================================================
    
    // --------------------------------------------------------
    // @Operation 注解
    // --------------------------------------------------------
    // Swagger 注解，描述接口功能
    // summary = "用户登录": 接口简介
    @Operation(summary = "用户登录")
    
    // --------------------------------------------------------
    // @PostMapping 注解
    // --------------------------------------------------------
    // 映射 HTTP POST 请求
    // "/login": 完整路径 = /api/auth/login
    // 【RESTful 设计】登录使用 POST，因为涉及状态修改
    @PostMapping("/login")
    
    // --------------------------------------------------------
    // 方法签名
    // --------------------------------------------------------
    // @Valid: 启用请求参数验证（如 @NotNull, @Email）
    // @RequestBody: 接收 JSON 请求体并转为 Java 对象
    // Result<AuthResponse>: 统一响应格式
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        // --------------------------------------------------------
        // 调用 Service 处理业务逻辑
        // --------------------------------------------------------
        // 【学习要点】Controller 不处理业务逻辑，只负责接收请求和返回响应
        AuthResponse response = authService.login(request);
        
        // --------------------------------------------------------
        // 返回统一响应格式
        // --------------------------------------------------------
        // Result.success(): 成功响应
        // 包含：code=200, message="成功", data=response
        return Result.success(response);
    }

    // ========================================================
    // 注册接口
    // ========================================================
    
    @Operation(summary = "用户注册")
    
    // --------------------------------------------------------
    // @PostMapping("/register")
    // --------------------------------------------------------
    // 映射 POST 请求到 /api/auth/register
    // 【RESTful 设计】注册使用 POST，创建新资源
    @PostMapping("/register")
    
    // --------------------------------------------------------
    // 方法签名
    // --------------------------------------------------------
    // RegisterRequest: 注册请求对象
    // 包含：username, email, password
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        // --------------------------------------------------------
        // 调用 Service 处理注册逻辑
        // --------------------------------------------------------
        // 注册成功后会创建用户和申请记录
        authService.register(request);
        
        // --------------------------------------------------------
        // 返回成功响应
        // --------------------------------------------------------
        // Result.success("注册成功，请等待管理员审核", null)
        // 包含：code=200, message="注册成功...", data=null
        return Result.success("注册成功，请等待管理员审核", null);
    }

    @Operation(summary = "重置管理员密码")
    @PostMapping("/reset-admin-password")
    public Result<String> resetAdminPassword() {
        try {
            User user = userRepository.findByUsername("admin").orElse(null);
            if (user == null) {
                return Result.error("管理员用户不存在");
            }
            user.setPasswordHash(passwordEncoder.encode("admin123"));
            userRepository.save(user);
            return Result.success("管理员密码已重置为 admin123");
        } catch (Exception e) {
            return Result.error("重置失败: " + e.getMessage());
        }
    }

    // ========================================================
    // RESTful API 设计原则
    // ========================================================
    // 
    // 【学习要点】RESTful API 设计最佳实践
    // 
    // 1. URL 设计：
    //    - 使用名词而非动词：/auth 而非 /authenticate
    //    - 使用复数表示集合：/users 而非 /user
    //    - 层级关系：/users/{id}/posts
    // 
    // 2. HTTP 方法：
    //    - GET: 查询资源（不修改数据）
    //    - POST: 创建资源
    //    - PUT: 更新资源（完整替换）
    //    - PATCH: 更新资源（部分修改）
    //    - DELETE: 删除资源
    // 
    // 3. 状态码：
    //    - 200 OK: 成功
    //    - 201 Created: 创建成功
    //    - 400 Bad Request: 请求参数错误
    //    - 401 Unauthorized: 未认证
    //    - 403 Forbidden: 无权限
    //    - 404 Not Found: 资源不存在
    //    - 500 Internal Server Error: 服务器错误
    // 
    // 4. 响应格式：
    //    - 统一格式便于前端处理
    //    - 包含：code, message, data
    // ========================================================
}
