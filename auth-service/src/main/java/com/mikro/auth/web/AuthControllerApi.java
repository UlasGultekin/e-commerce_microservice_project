package com.mikro.auth.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public interface AuthControllerApi {

    @PostMapping("/register")
    ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req);

    @PostMapping("/login")
    ResponseEntity<?> login(@Valid @RequestBody LoginRequest req);

    @GetMapping("/me")
    ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authorization);

    @PostMapping("/refresh")
    ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest req);

    @PostMapping("/logout")
    ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest req);

    record RegisterRequest(@NotBlank String username, @NotBlank String password, String role) {}
    record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    record RefreshTokenRequest(@NotBlank String refreshToken) {}
    record LogoutRequest(@NotBlank String refreshToken) {}
}


