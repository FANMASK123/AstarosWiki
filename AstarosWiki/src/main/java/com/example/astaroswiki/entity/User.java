package com.example.astaroswiki.entity;

import com.example.astaroswiki.enums.UserRole;
import com.example.astaroswiki.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//主键，自增
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.CREATOR; // 默认角色为CREATOR

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "extra_json", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    private String extraJson;

    @PrePersist
    protected void onCreate() { // 在用户实体被持久化到数据库之前调用
        // 【学习要点】用户实体包含用户ID、用户名、邮箱、密码哈希、角色（CREATOR）、状态（PENDING）、创建时间、更新时间、额外JSON字段
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { // 在用户实体被更新到数据库之前调用
        // 【学习要点】用户实体包含用户ID、用户名、邮箱、密码哈希、角色（CREATOR）、状态（PENDING）、创建时间、更新时间、额外JSON字段
        updatedAt = LocalDateTime.now();
    }
}
