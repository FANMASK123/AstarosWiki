package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.auth.AuthResponse;
import com.example.astaroswiki.dto.auth.LoginRequest;
import com.example.astaroswiki.dto.auth.RegisterRequest;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.enums.UserRole;
import com.example.astaroswiki.enums.UserStatus;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.UserRepository;
import com.example.astaroswiki.repository.UserRequestRepository;
import com.example.astaroswiki.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserRequestRepository userRequestRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setId(1L);
        activeUser.setUsername("testuser");
        activeUser.setPasswordHash("$2a$10$hashed");
        activeUser.setRole(UserRole.CREATOR);
        activeUser.setStatus(UserStatus.ACTIVE);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("correct");
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("正常登录 → 返回 token + 用户信息")
        void shouldReturnTokenWhenCredentialsValid() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("correct", activeUser.getPasswordHash())).thenReturn(true);
            when(jwtUtil.generateToken(1L, "testuser", "CREATOR")).thenReturn("jwt.token.here");

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getRole()).isEqualTo("CREATOR");
            assertThat(response.getUserId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("密码错误 → 抛 BusinessException")
        void shouldThrowWhenPasswordWrong() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("correct", activeUser.getPasswordHash())).thenReturn(false);

            assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名或密码错误");

            verify(jwtUtil, never()).generateToken(any(), anyString(), anyString());
        }

        @Test
        @DisplayName("用户待审核 → 拒绝登录")
        void shouldRejectPendingUser() {
            activeUser.setStatus(UserStatus.PENDING);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("correct", activeUser.getPasswordHash())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("待审核");
        }

        @Test
        @DisplayName("用户被禁用 → 拒绝登录")
        void shouldRejectDisabledUser() {
            activeUser.setStatus(UserStatus.DISABLED);
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("correct", activeUser.getPasswordHash())).thenReturn(true);

            assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已被禁用");
        }

        @Test
        @DisplayName("用户不存在 → 抛异常")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("正常注册 → 创建 PENDING 用户 + 申请记录")
        void shouldCreateUserWithPendingStatus() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setEmail("new@test.com");
            request.setPassword("pass123");

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
            when(passwordEncoder.encode("pass123")).thenReturn("$2a$10$encoded");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });

            authService.register(request);

            verify(userRepository).save(any(User.class));
            verify(userRequestRepository).save(any());
        }

        @Test
        @DisplayName("用户名已存在 → 抛异常")
        void shouldThrowWhenUsernameTaken() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("existing");
            request.setEmail("x@test.com");
            request.setPassword("pass");

            when(userRepository.existsByUsername("existing")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("用户名已存在");
        }

        @Test
        @DisplayName("邮箱已注册 → 抛异常")
        void shouldThrowWhenEmailTaken() {
            RegisterRequest request = new RegisterRequest();
            request.setUsername("new");
            request.setEmail("taken@test.com");
            request.setPassword("pass");

            when(userRepository.existsByUsername("new")).thenReturn(false);
            when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("邮箱已被注册");
        }
    }
}
