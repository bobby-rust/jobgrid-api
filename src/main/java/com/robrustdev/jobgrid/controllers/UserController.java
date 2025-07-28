package com.robrustdev.jobgrid.controllers;

import com.robrustdev.jobgrid.models.User;
import com.robrustdev.jobgrid.repositories.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository repo;

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<User> getUsers() {
        return repo.findAll();
    }
}
