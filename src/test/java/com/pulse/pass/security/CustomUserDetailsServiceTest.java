package com.pulse.pass.security;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserRoleRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserCredentialRepository credentialRepository;

    @Mock
    private UserRoleRepository roleRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void loadUserByUsername_existingUser_returnsPrincipalWithHashAndAuthorities() {
        // ARRANGE
        User user = user(1L, "andrea", "andrea@email.com", true);
        when(credentialRepository.findByUserIdentifier("andrea"))
                .thenReturn(List.of(new UserCredential(user, "stored-hash")));
        when(roleRepository.findByUser_Id(1L))
                .thenReturn(List.of(new UserRole(user, Role.USER), new UserRole(user, Role.STAFF)));

        // ACT
        UserDetails result = service.loadUserByUsername("andrea");

        // ASSERT
        assertThat(result).isInstanceOf(SecurityPrincipal.class);
        SecurityPrincipal principal = (SecurityPrincipal) result;
        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getUsername()).isEqualTo("andrea");
        assertThat(principal.getEmail()).isEqualTo("andrea@email.com");
        assertThat(principal.getPassword()).isEqualTo("stored-hash");
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_STAFF");
    }

    @Test
    void loadUserByUsername_inactiveUser_returnsDisabledPrincipal() {
        // ARRANGE
        User user = user(2L, "miguel", "miguel@email.com", false);
        when(credentialRepository.findByUserIdentifier("miguel"))
                .thenReturn(List.of(new UserCredential(user, "stored-hash")));
        when(roleRepository.findByUser_Id(2L)).thenReturn(List.of(new UserRole(user, Role.USER)));

        // ACT
        UserDetails result = service.loadUserByUsername("miguel");

        // ASSERT
        assertThat(result.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_userWithoutRoles_hasNoAuthorities() {
        // ARRANGE
        User user = user(3L, "laura", "laura@email.com", true);
        when(credentialRepository.findByUserIdentifier("laura"))
                .thenReturn(List.of(new UserCredential(user, "stored-hash")));
        when(roleRepository.findByUser_Id(3L)).thenReturn(List.of());

        // ACT
        UserDetails result = service.loadUserByUsername("laura");

        // ASSERT
        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_unknownIdentifier_throwsUsernameNotFound() {
        // ARRANGE
        when(credentialRepository.findByUserIdentifier("nobody")).thenReturn(List.of());

        // ACT
        Throwable thrown = catchThrowable(() -> service.loadUserByUsername("nobody"));

        // ASSERT
        assertThat(thrown).isInstanceOf(UsernameNotFoundException.class);
        assertThat(thrown.getMessage()).doesNotContain("nobody");
        verify(roleRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void loadUserByUsername_ambiguousIdentifier_throwsUsernameNotFound() {
        // ARRANGE
        User first = user(4L, "bob@email.com", "bob.one@email.com", true);
        User second = user(5L, "bob", "bob@email.com", true);
        when(credentialRepository.findByUserIdentifier("bob@email.com"))
                .thenReturn(List.of(new UserCredential(first, "hash-1"), new UserCredential(second, "hash-2")));

        // ACT
        Throwable thrown = catchThrowable(() -> service.loadUserByUsername("bob@email.com"));

        // ASSERT
        assertThat(thrown).isInstanceOf(UsernameNotFoundException.class);
        verify(roleRepository, never()).findByUser_Id(anyLong());
    }

    @Test
    void loadPrincipalById_existingUser_returnsPrincipalWithoutHash() {
        // ARRANGE
        User user = user(1L, "andrea", "andrea@email.com", true);
        when(credentialRepository.findWithUserByUserId(1L))
                .thenReturn(Optional.of(new UserCredential(user, "stored-hash")));
        when(roleRepository.findByUser_Id(1L)).thenReturn(List.of(new UserRole(user, Role.ADMIN)));

        // ACT
        SecurityPrincipal principal = service.loadPrincipalById(1L);

        // ASSERT
        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getPassword()).isNull();
        assertThat(principal.isEnabled()).isTrue();
        assertThat(principal.getAuthorities().stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadPrincipalById_inactiveUser_returnsDisabledPrincipal() {
        // ARRANGE
        User user = user(2L, "miguel", "miguel@email.com", false);
        when(credentialRepository.findWithUserByUserId(2L))
                .thenReturn(Optional.of(new UserCredential(user, "stored-hash")));
        when(roleRepository.findByUser_Id(2L)).thenReturn(List.of(new UserRole(user, Role.USER)));

        // ACT + ASSERT
        assertThat(service.loadPrincipalById(2L).isEnabled()).isFalse();
    }

    @Test
    void loadPrincipalById_missingUser_throwsUsernameNotFound() {
        // ARRANGE
        when(credentialRepository.findWithUserByUserId(99L)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.loadPrincipalById(99L));

        // ASSERT
        assertThat(thrown).isInstanceOf(UsernameNotFoundException.class);
        verify(roleRepository, never()).findByUser_Id(anyLong());
    }

    private User user(Long id, String username, String email, boolean active) {
        User user = new User(username, email, active);
        user.setId(id);
        return user;
    }
}