package com.example.identityservice.service;

import com.example.identityservice.dto.LoginRequest;
import com.example.identityservice.dto.LoginResponse;
import com.example.identityservice.dto.RegisterRequest;
import com.example.identityservice.dto.UserResponse;
import com.example.identityservice.entity.User;
import com.example.identityservice.exception.BadCredentialsException;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ========== REGISTER ==========
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : "USER")
                .build();

        User saved = userRepository.save(user);

        return UserResponse.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .role(saved.getRole())
                .build();
    }

    // ========== LOGIN (STATELESS) ==========
    /**
     * Xác thực người dùng và trả về JWT nếu hợp lệ.
     * - KHÔNG tạo session
     * - KHÔNG lưu trạng thái đăng nhập ở server
     * - Lỗi luôn trả "Bad credentials" (không phân biệt user không tồn tại / sai pass)
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        // 1. Tìm user trong DB. Nếu không có → vẫn ném BadCredentials
        //    (để tránh attacker dò username: "user không tồn tại" vs "sai mật khẩu")
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(BadCredentialsException::new);

        // 2. So khớp mật khẩu bằng BCrypt (KHÔNG dùng equals)
        //    passwordEncoder.matches() tự lấy salt từ hash và so sánh an toàn
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException();
        }

        // 3. Sinh JWT — đây là "chứng minh thư" thay cho session
        String token = jwtUtil.generateToken(user);

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(3600)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    // ========== HELPER cho test-token (Bài 2) ==========
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User không tồn tại: " + username));
    }
}