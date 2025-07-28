package com.robrustdev.jobgrid.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RegisterRequest {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
