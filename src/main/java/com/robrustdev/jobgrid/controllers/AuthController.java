package com.robrustdev.jobgrid.controllers;

import com.robrustdev.jobgrid.dtos.ApiResponse;
import com.robrustdev.jobgrid.dtos.Tokens;
import com.robrustdev.jobgrid.dtos.requests.LoginRequest;
import com.robrustdev.jobgrid.dtos.requests.RegisterRequest;
import com.robrustdev.jobgrid.dtos.responses.UserDTO;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.services.TokenService;
import com.robrustdev.jobgrid.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    @Value("${jwt.refresh.expiration-ms}")
    private long refreshTokenExpirationMs;
    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Object>> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, "User successfully registered", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Object>> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        User user = userService.login(request.getEmail(), request.getPassword());

        Tokens tokens = tokenService.createTokens(user);
        setTokens(response, tokens);

        return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        System.out.println("Getting current user");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized", null));
        }

        User user = (User) auth.getPrincipal();

        UserDTO userDTO = new UserDTO(user.getId(), user.getEmail(), user.getRoles());
        return ResponseEntity.ok(new ApiResponse<>(true, "User info retrieved", userDTO));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Object>> refreshTokens(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("Refreshing token");
        Tokens newTokens = tokenService.refreshJwt(request, response);
        setTokens(response, newTokens);
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed", null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletResponse response) {
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // deletes the cookie
        response.addCookie(jwtCookie);

        // Clear refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0); // deletes the cookie
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null));
    }

    private void setTokens(HttpServletResponse response, Tokens tokens) {
        // Create and set JWT cookie
        Cookie jwtCookie = new Cookie("jwt", tokens.getJwt());
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        int maxAccessTokenAgeSeconds = (int) (jwtExpirationMs / 1000);
        jwtCookie.setMaxAge(maxAccessTokenAgeSeconds);
        response.addCookie(jwtCookie);

        // Create and set refresh token cookie
        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        int maxRefreshTokenAgeSeconds = (int) (refreshTokenExpirationMs / 1000);
        refreshCookie.setMaxAge(maxRefreshTokenAgeSeconds);
        response.addCookie(refreshCookie);
    }
}
