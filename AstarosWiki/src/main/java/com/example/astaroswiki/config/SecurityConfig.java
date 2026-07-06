// ================================================================
// SecurityConfig - Spring Security 安全配置
// ================================================================
// 【学习要点】
// 1. 职责：配置 Spring Security 安全策略
// 2. 功能：JWT 认证、权限控制、跨域配置、密码加密
// 3. 位置：Spring Boot 安全配置类
// 4. 重要：整个后端应用的安全核心配置
// ================================================================

package com.example.astaroswiki.config;

// --------------------------------------------------------
// 导入依赖
// --------------------------------------------------------
import com.example.astaroswiki.security.JwtAuthenticationFilter;  // JWT 认证过滤器
import lombok.RequiredArgsConstructor;  // Lombok：生成构造器
import org.springframework.context.annotation.Bean;  // 声明 Bean
import org.springframework.context.annotation.Configuration;  // 标记为配置类
import org.springframework.security.authentication.AuthenticationManager;  // 认证管理器
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;  // 认证配置
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;  // 启用方法级权限注解
import org.springframework.security.config.annotation.web.builders.HttpSecurity;  // HTTP 安全配置
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;  // 启用 Web 安全
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;  // HTTP 配置器
import org.springframework.security.config.http.SessionCreationPolicy;  // Session 策略
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  // BCrypt 密码加密
import org.springframework.security.crypto.password.PasswordEncoder;  // 密码加密接口
import org.springframework.security.web.SecurityFilterChain;  // 安全过滤器链
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;  // 用户名密码认证过滤器
import org.springframework.web.cors.CorsConfiguration;  // CORS 配置
import org.springframework.web.cors.CorsConfigurationSource;  // CORS 配置源
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;  // 基于 URL 的 CORS 配置源

import java.util.Arrays;  // 数组工具类

// ================================================================
// 类级别注解
// ================================================================
@Configuration  // 标记为配置类，Spring 会自动扫描并加载
@EnableWebSecurity  // 启用 Spring Security Web 安全
@EnableMethodSecurity  // 启用方法级安全注解（如 @PreAuthorize）
@RequiredArgsConstructor  // Lombok：根据 final 字段自动生成构造器
public class SecurityConfig {

    // ========================================================
    // 依赖注入
    // ========================================================
    // 【学习要点】JwtAuthenticationFilter：JWT 认证过滤器
    // 用于拦截请求，验证 JWT Token，设置用户认证信息
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ========================================================
    // 安全过滤器链配置
    // ========================================================
    // 【学习要点】@Bean：声明为 Spring Bean，整个应用的安全策略
    // SecurityFilterChain：定义了请求的安全过滤规则链
    private static final long CORS_MAX_AGE_SECONDS = 3600L;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // --------------------------------------------------------
            // 1. 禁用 CSRF 防护
            // --------------------------------------------------------
            // 【学习要点】CSRF：跨站请求伪造攻击
            // 因为我们使用 JWT 无状态认证，不需要 CSRF Token，所以禁用
            // 注意：生产环境如果用 Session 认证，需要启用 CSRF
            .csrf(AbstractHttpConfigurer::disable)

            // --------------------------------------------------------
            // 2. 配置 CORS（跨域资源共享）
            // --------------------------------------------------------
            // 【学习要点】CORS：允许前端跨域请求
            // 前端（Vue）运行在不同端口，需要后端允许跨域
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // --------------------------------------------------------
            // 3. 配置 Session 管理策略
            // --------------------------------------------------------
            // 【学习要点】STATELESS：无状态策略
            // JWT 认证不依赖 Session，每个请求都携带 Token
            // 优点：不占用服务器资源，易于扩展
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // --------------------------------------------------------
            // 4. 配置请求授权规则
            // --------------------------------------------------------
            // 【学习要点】authorizeHttpRequests：配置哪些请求需要认证
            .authorizeHttpRequests(auth -> auth
                // 认证相关接口：所有人都可以访问（登录、注册）
                .requestMatchers("/api/auth/**").permitAll()
                
                // 公开内容接口：所有人都可以访问（浏览页面、分类、标签）
                .requestMatchers("/api/pages/**", "/api/categories/**", "/api/tags/**").permitAll()
                .requestMatchers("/api/files/**").authenticated()
                .requestMatchers("/uploads/**").permitAll()
                
                // 用户相关接口：需要登录认证
                .requestMatchers("/api/users/**").authenticated()
                
                // Swagger 文档接口：所有人都可以访问（开发时）
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/doc.html").permitAll()
                
                // 管理员接口：只有 ADMIN 角色可以访问
                // 【学习要点】hasRole("ADMIN")：检查用户角色
                // 注意：角色名会自动添加 "ROLE_" 前缀
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 其他所有请求：需要登录认证
                .anyRequest().authenticated()
            )

            // --------------------------------------------------------
            // 5. 添加 JWT 过滤器
            // --------------------------------------------------------
            // 【学习要点】addFilterBefore：在指定过滤器之前添加我们的过滤器
            // UsernamePasswordAuthenticationFilter：Spring Security 默认的用户名密码认证过滤器
            // 我们希望 JWT 过滤器先执行，所以添加到它之前
            .addFilterBefore(
                jwtAuthenticationFilter,  // 我们的 JWT 过滤器
                UsernamePasswordAuthenticationFilter.class  // 在用户名密码认证之前
            );

        // 构建并返回安全过滤器链
        return http.build();
    }

    // ========================================================
    // CORS 跨域配置
    // ========================================================
    // 【学习要点】CORS 配置：允许前端跨域访问后端 API
    // 解决浏览器同源策略导致的跨域问题
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // 创建 CORS 配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        
        // --------------------------------------------------------
        // 允许的来源
        // --------------------------------------------------------
        // 【学习要点】allowedOriginPatterns：允许的请求来源
        // "*" 表示允许所有来源（开发环境）
        // 生产环境应该指定具体域名，如："http://localhost:5173"
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // --------------------------------------------------------
        // 允许的 HTTP 方法
        // --------------------------------------------------------
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 说明：
        // GET：查询
        // POST：创建
        // PUT：更新
        // DELETE：删除
        // OPTIONS：预检请求（浏览器自动发送）
        
        // --------------------------------------------------------
        // 允许的请求头
        // --------------------------------------------------------
        // "*" 表示允许所有请求头
        // 例如：Content-Type, Authorization 等
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // --------------------------------------------------------
        // 是否允许携带凭证
        // --------------------------------------------------------
        // 【学习要点】allowCredentials：是否允许携带 Cookie
        // 如果设置为 true，allowedOriginPatterns 不能为 "*"
        configuration.setAllowCredentials(true);
        
        // --------------------------------------------------------
        // 预检请求缓存时间
        // --------------------------------------------------------
        // 3600 秒：浏览器在 1 小时内不会再次发送 OPTIONS 预检请求
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

        // 创建基于 URL 的 CORS 配置源
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // 对所有路径应用此 CORS 配置
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    // ========================================================
    // 密码加密器配置
    // ========================================================
    // 【学习要点】密码加密：用户密码不能明文存储
    // BCrypt：业界标准的密码哈希算法，自带盐值
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder：BCrypt 算法实现
        // 特点：
        // 1. 自动生成随机盐值
        // 2. 每次加密结果都不同（因为盐值不同）
        // 3. 验证时自动提取盐值进行比对
        return new BCryptPasswordEncoder();
    }

    // ========================================================
    // 认证管理器配置
    // ========================================================
    // 【学习要点】AuthenticationManager：认证管理器
    // 用于处理认证逻辑（如登录验证）
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        // 从配置中获取 AuthenticationManager
        return config.getAuthenticationManager();
    }
}

// ================================================================
// 【总结】SecurityConfig 的核心配置
// ================================================================
// 1. 安全策略：无状态（JWT）+ CSRF 禁用
// 2. 权限控制：公开接口 vs 需认证接口 vs 管理员接口
// 3. 跨域支持：允许前端跨域访问
// 4. 密码加密：BCrypt 算法
// 5. JWT 认证：在UsernamePasswordAuthenticationFilter 之前执行
// ================================================================
