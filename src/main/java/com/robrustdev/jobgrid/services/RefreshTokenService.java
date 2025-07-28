package com.robrustdev.jobgrid.services;

import com.robrustdev.jobgrid.models.RefreshToken;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.repositories.RefreshTokenRepository;
import com.robrustdev.jobgrid.repositories.UserRepository;
import com.robrustdev.jobgrid.security.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.sql.Ref;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {
    @Value("${jwt.refresh.expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtils jwtUtils;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtUtils jwtUtils) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtils = jwtUtils;
    }

    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        return refreshTokenRepository.save(refreshToken);
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);

        findByToken(refreshToken)
                .map(this::verifyExpiration)
                .map(RefreshToken::getUser)
                .ifPresentOrElse(user->{
                    String newAccessToken = jwtUtils.generateToken(user);
                    setAccessTokenCookie(response, newAccessToken);

                    RefreshToken newRefreshToken = createRefreshToken(user);
                    setRefreshTokenCookie(response, newRefreshToken.getToken());
                }, () -> {
                    throw new RuntimeException("Refresh token not found or expired");
                });

    }

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtils.getJwtExpirationMs() / 1000)); // Match access token expiry
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshTokenDurationMs / 1000));
        response.addCookie(cookie);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expired");
        }

        return token;
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals("refreshToken")) {
                return cookie.getValue();
            }
        }

        return null;
    }
}