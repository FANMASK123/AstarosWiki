// ================================================================
// JwtUtil - JWT 工具类
// ================================================================
// 【学习要点】
// 1. 职责：提供 JWT Token 的生成、解析、验证功能
// 2. 功能：创建 Token、解析 Token、获取用户信息、验证 Token 有效性
// 3. 位置：Spring Security 认证体系的一部分
// 4. 核心：JWT（JSON Web Token）是一种自包含的令牌格式
// ================================================================

package com.example.astaroswiki.util;

// --------------------------------------------------------
// 导入依赖
// --------------------------------------------------------
import io.jsonwebtoken.Claims;  // JWT 声明对象（包含用户信息）
import io.jsonwebtoken.Jwts;  // JWT 库的主类
import io.jsonwebtoken.security.Keys;  // 密钥工具类
import org.springframework.beans.factory.annotation.Value;  // 读取配置值
import org.springframework.stereotype.Component;  // 标记为组件

import javax.crypto.SecretKey;  // 密钥接口
import java.nio.charset.StandardCharsets;  // 字符编码
import java.util.Date;  // 日期类
import java.util.HashMap;  // HashMap
import java.util.Map;  // Map 接口

// ================================================================
// 类级别注解
// ================================================================
@Component  // 标记为 Spring 组件
public class JwtUtil {

    // ========================================================
    // 配置注入
    // ========================================================
    // 【学习要点】@Value：从配置文件读取值
    // 读取 application.yml 中的 jwt.secret 配置
    @Value("${jwt.secret}")
    private String secret;

    // Token 过期时间（毫秒），从配置读取
    @Value("${jwt.expiration}")
    private Long expiration;

    // ========================================================
    // 获取密钥
    // ========================================================
    // 【学习要点】HMAC-SHA 密钥
    // JWT 使用 HMAC-SHA 算法签名，需要一个密钥
    private SecretKey getSecretKey() {
        // 将字符串密钥转换为字节数组，再用 Keys 工具转换为密钥对象
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ========================================================
    // 生成 Token
    // ========================================================
    // 【学习要点】JWT 结构：Header.Payload.Signature
    // 1. Header：算法和类型
    // 2. Payload：声明（用户信息）
    // 3. Signature：签名（防篡改）
    public String generateToken(Long userId, String username, String role) {
        // --------------------------------------------------------
        // 1. 创建声明（Payload 中存储的数据）
        // --------------------------------------------------------
        // 【学习要点】Claims：JWT 的声明，包含用户自定义的数据
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);     // 用户 ID
        claims.put("username", username); // 用户名
        claims.put("role", role);         // 角色

        // --------------------------------------------------------
        // 2. 构建 JWT
        // --------------------------------------------------------
        return Jwts.builder()
                // 设置声明（Payload）
                .claims(claims)
                // 设置主题（subject = 用户名）
                .subject(username)
                // 设置签发时间
                .issuedAt(new Date())
                // 设置过期时间 = 当前时间 + 过期时长
                .expiration(new Date(System.currentTimeMillis() + expiration))
                // 使用密钥签名
                .signWith(getSecretKey())
                // 生成 Token 字符串
                .compact();
    }

    // ========================================================
    // 解析 Token
    // ========================================================
    // 【学习要点】将 Token 字符串解析为 Claims 对象
    public Claims parseToken(String token) {
        return Jwts.parser()
                // 设置验证密钥
                .verifyWith(getSecretKey())
                // 构建解析器
                .build()
                // 解析 Token（会验证签名）
                .parseSignedClaims(token)
                // 获取 Payload（声明）
                .getPayload();
    }

    // ========================================================
    // 获取用户 ID
    // ========================================================
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);  // 从声明中获取 userId
    }

    // ========================================================
    // 获取用户名
    // ========================================================
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();  // getSubject() 获取主题（用户名）
    }

    // ========================================================
    // 获取角色
    // ========================================================
    public String getRole(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);  // 从声明中获取角色
    }

    // ========================================================
    // 检查 Token 是否过期
    // ========================================================
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        // getExpiration() 获取过期时间，before() 判断是否在当前时间之前
        return claims.getExpiration().before(new Date());
    }

    // ========================================================
    // 验证 Token（核心验证方法）
    // ========================================================
    // 【学习要点】验证 Token 是否有效
    // 1. 能够正确解析（签名正确）
    // 2. 没有过期
    public boolean validateToken(String token) {
        try {
            parseToken(token);  // 如果签名错误会抛出异常
            return !isTokenExpired(token);  // 检查是否过期
        } catch (Exception e) {
            // 任何异常都表示 Token 无效
            // 包括：签名错误、格式错误、过期等
            return false;
        }
    }
}

// ================================================================
// 【总结】JWT 工作原理
// ================================================================
// 1. 生成 Token（登录时）：
//    用户信息 → JwtUtil.generateToken() → Token 字符串
//
// 2. 携带 Token（每次请求）：
//    Headers: { Authorization: Bearer <token> }
//
// 3. 验证 Token（过滤器）：
//    JwtAuthenticationFilter → JwtUtil.validateToken() → 通过/失败
//
// 4. 解析 Token（获取用户信息）：
//    JwtUtil.getUserId() / getUsername() / getRole()
//
// JWT 优点：
// - 无状态：服务器不存储 Token，节省资源
// - 自包含：Token 本身包含用户信息
// - 可验证：签名保证 Token 不能被伪造
// ================================================================
