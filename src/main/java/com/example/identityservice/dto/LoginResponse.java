package com.example.identityservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String tokenType;   // "Bearer"
    private long expiresIn;     // giây
    private String username;
    private String role;
}
