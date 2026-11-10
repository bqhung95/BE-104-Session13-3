package com.example.identityservice.controller;

import com.example.identityservice.dto.*;
import com.example.identityservice.entity.User;
import com.example.identityservice.service.AuthService;
import com.example.identityservice.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Response 200: { accessToken, tokenType, expiresIn, username, role }
     * Response 401: { "error": "Bad credentials", "status": 401 }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/test-token")
    public ResponseEntity<Map<String, Object>> testToken(@RequestParam String username) {
        User user = authService.findByUsername(username);
        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "role", user.getRole(),
                "tokenType", "Bearer",
                "expiresIn", 3600,
                "accessToken", token
        ));
    }
}
