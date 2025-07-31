package com.robrustdev.jobgrid.services;

import com.robrustdev.jobgrid.exceptions.EmailInUseException;
import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
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

        return user;
    }
}
