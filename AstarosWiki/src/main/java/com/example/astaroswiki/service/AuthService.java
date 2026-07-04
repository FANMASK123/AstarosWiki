package com.example.astaroswiki.service;

import com.example.astaroswiki.dto.auth.AuthResponse;
import com.example.astaroswiki.dto.auth.LoginRequest;
import com.example.astaroswiki.dto.auth.RegisterRequest;
import com.example.astaroswiki.entity.User;
import com.example.astaroswiki.entity.UserRequest;
import com.example.astaroswiki.enums.RequestStatus;
import com.example.astaroswiki.enums.RequestType;
import com.example.astaroswiki.enums.UserRole;
import com.example.astaroswiki.enums.UserStatus;
import com.example.astaroswiki.exception.BusinessException;
import com.example.astaroswiki.repository.UserRepository;
import com.example.astaroswiki.repository.UserRequestRepository;
import com.example.astaroswiki.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRequestRepository userRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new BusinessException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new BusinessException("账户待审核，请等待管理员审批");
        }

        if (user.getStatus() == UserStatus.DISABLED) {
            throw new BusinessException("账户已被禁用");
        }

        String token = jwtUtil.generateToken(
            user.getId(), user.getUsername(), user.getRole().name()
        );

        return new AuthResponse(token, user.getUsername(), user.getRole().name(), user.getId());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CREATOR);
        user.setStatus(UserStatus.PENDING);

        user = userRepository.save(user);

        UserRequest userRequest = new UserRequest();
        userRequest.setUserId(user.getId());
        userRequest.setRequestType(RequestType.CREATOR_REGISTRATION);
        userRequest.setStatus(RequestStatus.PENDING);

        userRequestRepository.save(userRequest);
    }

    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
