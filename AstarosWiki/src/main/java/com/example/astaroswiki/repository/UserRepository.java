package com.example.astaroswiki.repository;

// ============================================================
// UserRepository - 用户数据访问层接口
// ============================================================
// 
// 【Spring Data JPA 知识点】
// 1. JpaRepository: Spring Data JPA 提供的基础接口
// 2. 方法命名规则查询: 根据方法名自动生成 SQL
// 3. Optional: Java 8 引入的容器类，避免空指针异常
// 
// 【推荐阅读】
// https://www.baeldung.com/the-persistence-layer-with-spring-data-jpa
// ============================================================

import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// --------------------------------------------------------
// @Repository 注解
// --------------------------------------------------------
// 标记这是一个数据访问层组件
// Spring 会自动扫描并注册为 Bean
// 【学习要点】JpaRepository<User, Long> 表示 User 实体的数据库操作，Long 表示主键类型，在该部分的实际作用是用于存储用户信息，每个用户都有一个唯一的 Long 类型的主键 id，用于唯一标识每个用户
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ========================================================
    // Spring Data JPA 方法命名规则查询
    // ========================================================
    // 【学习要点】Spring Data JPA 根据方法名自动生成查询
    // 
    // 方法名规则：
    // - findBy: 查询方法前缀
    // - Xxx: 属性名（如 Username）
    // - 返回类型: Optional<User> 或 User 或 List<User>
    // 
    // 生成的 SQL 示例：
    // findByUsername → SELECT * FROM users WHERE username = ?
    // ========================================================

    // --------------------------------------------------------
    // 根据用户名查询用户
    // --------------------------------------------------------
    // 方法名: findByUsername
    // 生成SQL: SELECT * FROM users WHERE username = ?
    // 返回类型: Optional<User> - 可能不存在，避免空指针
    // 与User实体中的username字段对应，根据用户名查询用户信息
    Optional<User> findByUsername(String username);

    // --------------------------------------------------------
    // 根据邮箱查询用户
    // --------------------------------------------------------
    // 方法名: findByEmail
    // 生成SQL: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // --------------------------------------------------------
    // 检查用户名是否存在
    // --------------------------------------------------------
    // 方法名: existsByUsername
    // 生成SQL: SELECT COUNT(*) FROM users WHERE username = ?
    // 返回类型: boolean - true表示存在，false表示不存在
    // 【学习要点】existsBy 比 findBy + null判断 更高效
    boolean existsByUsername(String username);

    // --------------------------------------------------------
    // 检查邮箱是否存在
    // --------------------------------------------------------
    // 方法名: existsByEmail
    // 生成SQL: SELECT COUNT(*) FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // --------------------------------------------------------
    // 统计指定状态的用户数量
    // --------------------------------------------------------
    // 方法名: countByStatus
    // 生成SQL: SELECT COUNT(*) FROM users WHERE status = ?
    // 【学习要点】可用于管理员统计待审核用户数量
    long countByStatus(UserStatus status);

    // ========================================================
    // JpaRepository 继承的方法（不需要定义）
    // ========================================================
    // JpaRepository<User, Long> 已经提供了以下方法：
    // 
    // - save(User user): 保存或更新用户
    // - findById(Long id): 根据ID查询用户
    // - findAll(): 查询所有用户
    // - deleteById(Long id): 根据ID删除用户
    // - delete(User user): 删除指定用户
    // - count(): 统计用户总数
    // - existsById(Long id): 检查ID是否存在
    // 
    // 【学习要点】继承 JpaRepository 后，无需编写基础CRUD方法
    // ========================================================
}
