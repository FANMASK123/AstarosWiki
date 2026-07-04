// ================================================================
// CustomUserDetailsService - Spring Security 用户详情服务
// ================================================================
// 【学习要点】
// 1. 职责：将数据库用户转换为 Spring Security 可用的用户对象
// 2. 功能：实现 UserDetailsService 接口，提供用户认证信息
// 3. 位置：Spring Security 认证流程的一部分
// 4. 作用：当进行身份验证时，Spring Security 会调用此服务获取用户信息
// ================================================================

package com.example.astaroswiki.security;

// --------------------------------------------------------
// 导入依赖
// --------------------------------------------------------
import com.example.astaroswiki.entity.User;  // 用户实体类
import com.example.astaroswiki.repository.UserRepository;  // 用户数据访问层
import lombok.RequiredArgsConstructor;  // Lombok：生成构造器
import org.springframework.security.core.authority.SimpleGrantedAuthority;  // 权限对象
import org.springframework.security.core.userdetails.UserDetails;  // Spring Security 用户详情接口
import org.springframework.security.core.userdetails.UserDetailsService;  // 用户详情服务接口
import org.springframework.security.core.userdetails.UsernameNotFoundException;  // 用户不存在异常
import org.springframework.stereotype.Service;  // 标记为 Service 层组件

import java.util.Collections;  // 集合工具类

// ================================================================
// 类级别注解
// ================================================================
@Service  // 标记为 Spring 服务组件
@RequiredArgsConstructor  // Lombok：根据 final 字段自动生成构造器
public class CustomUserDetailsService implements UserDetailsService {

    // ========================================================
    // 依赖注入
    // ========================================================
    // 【学习要点】UserRepository：用于查询数据库中的用户
    private final UserRepository userRepository;

    // ========================================================
    // 加载用户详情（核心方法）
    // ========================================================
    // 【学习要点】UserDetailsService 接口的唯一方法
    // Spring Security 在认证时会自动调用此方法
    // 参数：username - 用户名（登录时输入的用户名）
    // 返回：UserDetails - Spring Security 标准的用户对象
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // --------------------------------------------------------
        // 1. 从数据库查询用户
        // --------------------------------------------------------
        // 【学习要点】findByUsername：根据用户名查询用户
        // 如果用户不存在，抛出 UsernameNotFoundException
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        // --------------------------------------------------------
        // 2. 转换为 Spring Security 的 UserDetails 对象
        // --------------------------------------------------------
        // 【学习要点】UserDetails：Spring Security 标准的用户对象
        // 包含：用户名、密码、权限列表
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),  // 用户名
                user.getPasswordHash(),  // 密码（数据库中存储的哈希值）
                // 权限列表：将用户角色转换为权限对象
                // 【学习要点】SimpleGrantedAuthority：权限对象
                // 角色名会自动添加 "ROLE_" 前缀（如 "ADMIN" → "ROLE_ADMIN"）
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                )
        );
    }
}

// ================================================================
// 【总结】CustomUserDetailsService 的作用
// ================================================================
// 认证流程：
// 1. 用户登录 → 提交用户名密码
// 2. Spring Security 调用 loadUserByUsername(username)
// 3. 此方法查询数据库，返回 UserDetails 对象
// 4. Spring Security 比对密码是否正确
// 5. 认证成功 → 创建 SecurityContext
// 6. 认证失败 → 抛出异常
// ================================================================
