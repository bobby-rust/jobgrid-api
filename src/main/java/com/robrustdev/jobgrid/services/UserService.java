package com.robrustdev.jobgrid.services;

import com.robrustdev.jobgrid.exceptions.EmailInUseException;
import com.robrustdev.jobgrid.models.RefreshToken;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.repositories.UserRepository;
import com.robrustdev.jobgrid.security.jwt.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RefreshTokenService refreshTokenService) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
    }

    public void register(String email, String rawPassword) {
        if (repo.findByEmail(email).isPresent()) {
            throw new EmailInUseException("User with email already exists");
        }

        String hashed = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setRoles(List.of("ROLE_USER"));
        repo.save(user);
    }

    public User login(String email, String rawPassword) {
        User user = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String jwt = jwtUtils.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);



        return user;
    }

    private void setJwtCookie(String jwt) {

    }

    private void setRefreshTokenCookie()
}
