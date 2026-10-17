package com.pulse.pass.security;

import com.pulse.pass.domain.Role;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPrincipalTest {

    @Test
    void shouldPrefixRolesWithRoleAuthority() {
        SecurityPrincipal principal = principal(true, Set.of(Role.USER, Role.ADMIN));

        assertThat(principal.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void shouldMirrorActiveFlagInIsEnabled() {
        assertThat(principal(true, Set.of(Role.USER)).isEnabled()).isTrue();
        assertThat(principal(false, Set.of(Role.USER)).isEnabled()).isFalse();
    }

    @Test
    void shouldEraseHashAfterAuthentication() {
        SecurityPrincipal principal = principal(true, Set.of(Role.USER));

        principal.eraseCredentials();

        assertThat(principal.getPassword()).isNull();
    }

    private SecurityPrincipal principal(boolean active, Set<Role> roles) {
        return new SecurityPrincipal(1L, "andrea", "andrea@email.com", "stored-hash", active, roles);
    }
}