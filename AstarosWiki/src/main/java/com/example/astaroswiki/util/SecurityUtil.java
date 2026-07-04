// ================================================================
// SecurityUtil - Security 工具类
// ================================================================
// 【学习要点】
// 1. 职责：提供获取当前登录用户信息的静态方法
// 2. 功能：获取当前用户 ID、用户名、判断是否登录
// 3. 位置：Controller、Service 等层中获取当前用户信息
// 4. 原理：从 SecurityContextHolder 中获取认证信息
// ================================================================

package com.example.astaroswiki.util;

// --------------------------------------------------------
// 导入依赖
// --------------------------------------------------------
import org.springframework.security.core.Authentication;  // 认证接口
import org.springframework.security.core.context.SecurityContextHolder;  // Security 上下文持有者
import org.springframework.stereotype.Component;  // 标记为组件

// ================================================================
// 类级别注解
// ================================================================
@Component  // 标记为 Spring 组件
public class SecurityUtil {

    // ========================================================
    // 获取当前登录用户的 ID
    // ========================================================
    // 【学习要点】从 SecurityContextHolder 获取当前认证信息
    // 用于：Controller 获取当前登录用户的 ID，进行权限判断
    public static Long getCurrentUserId() {
        // --------------------------------------------------------
        // 1. 获取 Security 上下文
        // --------------------------------------------------------
        // 【学习要点】SecurityContextHolder：存储当前线程的认证信息
        // 每个请求都有一个 SecurityContext，由 JwtAuthenticationFilter 设置
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // --------------------------------------------------------
        // 2. 检查认证信息和用户类型
        // --------------------------------------------------------
        // 【学习要点】getPrincipal()：获取认证主体
        // 在我们的项目中，JwtAuthenticationFilter 将用户 ID 作为 Principal
        // instanceof Long：检查是否是 Long 类型（用户 ID）
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            // 转换为 Long 并返回
            return (Long) authentication.getPrincipal();
        }

        // 如果没有认证或类型不匹配，返回 null
        return null;
    }

    // ========================================================
    // 获取当前登录用户的用户名
    // ========================================================
    // 【学习要点】getName()：获取认证主体的名称（用户名）
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            // getName()：获取用户名
            // 对于我们的项目，返回的是用户名
            return authentication.getName();
        }

        return null;
    }

    // ========================================================
    // 判断用户是否已登录
    // ========================================================
    // 【学习要点】isAuthenticated()：判断是否已认证
    // 用于：判断用户是否登录，控制页面显示等
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // --------------------------------------------------------
        // 判断条件
        // --------------------------------------------------------
        // 1. authentication != null：存在认证信息
        // 2. authentication.isAuthenticated()：已认证
        // 3. !"anonymousUser".equals(authentication.getPrincipal())：
        //    不是匿名用户（Spring Security 默认的匿名用户 principal 是 "anonymousUser"）
        return authentication != null 
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }
}

// ================================================================
// 【总结】SecurityUtil 使用场景
// ================================================================
// 1. 在 Controller 中获取当前用户 ID：
//    Long userId = SecurityUtil.getCurrentUserId();
//    adminService.doSomething(requestId, userId);
//
// 2. 在 Service 中判断权限：
//    Long currentUserId = SecurityUtil.getCurrentUserId();
//    if (!page.getAuthorId().equals(currentUserId)) {
//        throw new BusinessException("无权限");
//    }
//
// 3. 在前端判断登录状态：
//    后端返回 null 表示未登录
//    前端根据 null 判断显示登录/未登录按钮
// ================================================================
//
// 【注意】为什么用 Component 而不是 static 工具类？
// --------------------------------------------------------
// 虽然方法都是 static，但类上有 @Component
// 原因：这样可以通过依赖注入使用，且能被 Spring 管理
// 使用时直接调用静态方法即可：
//    SecurityUtil.getCurrentUserId();
// 不需要注入 SecurityUtil 对象
// ================================================================
