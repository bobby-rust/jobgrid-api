package com.robrustdev.jobgrid.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tokens {
    private String jwt;
    private String refreshToken;

    public Tokens() {}

    public Tokens(String jwt, String refreshToken) {
        this.jwt = jwt;
        this.refreshToken = refreshToken;
    }
}
