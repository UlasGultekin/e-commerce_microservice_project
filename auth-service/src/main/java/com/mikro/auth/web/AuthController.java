package com.mikro.auth.web;

import com.mikro.auth.exception.UserAlreadyExistsException;
import com.mikro.auth.jwt.JwtService;
import com.mikro.auth.refresh.RefreshToken;
import com.mikro.auth.refresh.RefreshTokenService;
import com.mikro.auth.user.User;
import com.mikro.auth.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerApi {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                         JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public ResponseEntity<?> register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new UserAlreadyExistsException("Username '" + req.username() + "' is already taken");
        }
        
        // Role belirleme stratejisi
        String userRole = determineUserRole(req.username(), req.role());
        
        User user = User.builder()
            .username(req.username())
            .passwordHash(passwordEncoder.encode(req.password()))
            .role(userRole)
            .build();
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("status", "registered", "role", userRole));
    }
    
    private String determineUserRole(String username, String requestedRole) {
        // Yalnızca iki rol: ROLE_CUSTOMER ve ROLE_SHOP_OWNER
        if (requestedRole != null) {
            String norm = requestedRole.trim().toLowerCase();
            // customer eşleşmeleri
            if (norm.equals("customer") || norm.equals("musteri") || norm.equals("müşteri") || norm.equals("role_customer") || norm.equals("role_user")) {
                return "ROLE_CUSTOMER";
            }
            // shop owner eşleşmeleri
            if (norm.equals("shop") || norm.equals("shop_owner") || norm.equals("store") || norm.equals("dukkan") || norm.equals("dükkan") || norm.equals("satici") || norm.equals("satıcı") || norm.equals("role_shop_owner")) {
                return "ROLE_SHOP_OWNER";
            }
            // Doğrudan ROLE_ prefix ile gönderildiyse
            if (isValidRole(requestedRole)) {
                return requestedRole;
            }
        }
        // Varsayılan: ROLE_CUSTOMER (güvenli seçim)
        return "ROLE_CUSTOMER";
    }

    private boolean isValidRole(String role) {
        return "ROLE_CUSTOMER".equals(role) || "ROLE_SHOP_OWNER".equals(role);
    }

    @Override
    public ResponseEntity<?> login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        
        String accessToken = jwtService.generateAccessToken(user.getUsername(), Map.of("role", user.getRole()));
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        
        return ResponseEntity.ok(Map.of(
                "access_token", accessToken,
                "refresh_token", refreshToken.getToken(),
                "token_type", "Bearer"
        ));
    }

    @Override
    public ResponseEntity<?> me(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        try {
            String token = authorization.substring(7);
            var claims = jwtService.parseToken(token);
            String username = claims.getSubject();
            Object role = claims.get("role");
            return ResponseEntity.ok(Map.of("username", username, "role", role));
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @Override
    public ResponseEntity<?> refresh(RefreshTokenRequest req) {
        try {
            Map<String, String> tokens = refreshTokenService.refreshAccessToken(req.refreshToken());
            return ResponseEntity.ok(tokens);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }
    }

    @Override
    public ResponseEntity<?> logout(LogoutRequest req) {
        refreshTokenService.revokeToken(req.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

}


