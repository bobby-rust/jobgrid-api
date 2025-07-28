package com.robrustdev.jobgrid.controllers;

import com.robrustdev.jobgrid.dtos.ApiResponse;
import com.robrustdev.jobgrid.dtos.requests.LoginRequest;
import com.robrustdev.jobgrid.dtos.requests.RegisterRequest;
import com.robrustdev.jobgrid.models.RefreshToken;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.services.RefreshTokenService;
import com.robrustdev.jobgrid.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserService userService, RefreshTokenService refreshTokenService) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, "User successfully registered", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        User user = userService.login(request.getEmail(), request.getPassword());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        String jwt = jwtUtils.

        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);

        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenService.refresh(request, response);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed", null));
    }
}
