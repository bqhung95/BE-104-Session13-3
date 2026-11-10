package com.example.identityservice.exception;

import org.springframework.security.core.AuthenticationException;

public class BadCredentialsException extends AuthenticationException {
    public BadCredentialsException() {
        super("Bad credentials");
    }
}
