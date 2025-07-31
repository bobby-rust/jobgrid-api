package com.robrustdev.jobgrid.services;

import com.robrustdev.jobgrid.dtos.Tokens;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.repositories.UserRepository;
import com.robrustdev.jobgrid.security.jwt.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    public TokenService(JwtUtils jwtUtils, UserRepository userRepository) {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    public Tokens createTokens(User user) {
        return new Tokens(jwtUtils.generateAccessToken(user), jwtUtils.generateAccessToken(user));
    }

    public Tokens refreshJwt(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookies(request);
        if (refreshToken == null || !jwtUtils.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or missing refresh token");
        }

        Long userId = Long.valueOf(jwtUtils.getUserIdFromToken(refreshToken));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(("User not found")));

        String newAccessToken  = jwtUtils.generateAccessToken(user);
        String newRefreshToken = jwtUtils.generateRefreshToken(user);

        return new Tokens(newAccessToken, newRefreshToken);
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