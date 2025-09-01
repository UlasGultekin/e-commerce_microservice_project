package com.mikro.auth.refresh;

import com.mikro.auth.jwt.JwtService;
import com.mikro.auth.user.User;
import com.mikro.auth.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final long refreshTokenExpirationDays;
    
    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${jwt.refresh-token-expiration-days:7}") long refreshTokenExpirationDays
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.refreshTokenExpirationDays = refreshTokenExpirationDays;
    }
    
    public RefreshToken createRefreshToken(String username) {
        // Revoke existing tokens for this user
        refreshTokenRepository.revokeByUsername(username);
        
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .expiryDate(Instant.now().plusSeconds(refreshTokenExpirationDays * 24 * 60 * 60))
                .createdAt(Instant.now())
                .revoked(false)
                .build();
        
        return refreshTokenRepository.save(refreshToken);
    }
    
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }
    
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request");
        }
        return token;
    }
    
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(refreshToken -> {
                    refreshToken.setRevoked(true);
                    refreshTokenRepository.save(refreshToken);
                });
    }
    
    public void revokeAllTokensForUser(String username) {
        refreshTokenRepository.revokeByUsername(username);
    }
    
    public Map<String, String> refreshAccessToken(String refreshToken) {
        RefreshToken token = findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        
        verifyExpiration(token);
        
        User user = userRepository.findByUsername(token.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate new access token
        String newAccessToken = jwtService.generateAccessToken(
                user.getUsername(), 
                Map.of("role", user.getRole())
        );
        
        return Map.of(
                "access_token", newAccessToken,
                "token_type", "Bearer"
        );
    }
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens(Instant.now());
    }
}
