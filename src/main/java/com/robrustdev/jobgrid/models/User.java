package com.robrustdev.jobgrid.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name="users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = List.of("ROLE_USER");

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;  // username is email in this app
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // no expiration logic yet
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // no lock logic yet
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // no credentials expiration logic yet
    }

    @Override
    public boolean isEnabled() {
        return true;  // no enable/disable logic yet
    }
}
