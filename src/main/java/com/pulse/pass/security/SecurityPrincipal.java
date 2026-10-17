package com.pulse.pass.security;

import com.pulse.pass.domain.Role;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SecurityPrincipal implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String username;
    private final String email;
    private final boolean active;
    private final List<GrantedAuthority> authorities;
    private String passwordHash;

    public SecurityPrincipal(Long id, String username, String email,
                             String passwordHash, boolean active, Set<Role> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.active = active;

        List<GrantedAuthority> granted = new ArrayList<>();
        for (Role role : roles) {
            granted.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }
        this.authorities = List.copyOf(granted);
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Una cuenta inactiva no puede autenticarse (DaoAuthenticationProvider lanza DisabledException)
    @Override
    public boolean isEnabled() {
        return active;
    }

    // Spring Security lo invoca tras autenticar, para que el hash no viaje en el SecurityContext
    @Override
    public void eraseCredentials() {
        this.passwordHash = null;
    }
}