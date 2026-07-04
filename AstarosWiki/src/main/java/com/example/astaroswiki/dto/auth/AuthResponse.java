package com.example.astaroswiki.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token; // 登录成功后返回的token，用于后续请求验证，token包含用户信息，如用户名、角色、用户ID等
    private String username; // 用户名
    private String role; // 角色
    private Long userId; // 用户ID
}
