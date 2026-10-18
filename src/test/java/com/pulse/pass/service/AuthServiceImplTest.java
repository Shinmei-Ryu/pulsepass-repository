package com.pulse.pass.service;

import com.pulse.pass.domain.Role;
import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;
import com.pulse.pass.dto.response.AuthenticatedUserResponse;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.security.SecurityPrincipal;
import com.pulse.pass.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String IDENTIFIER = "andrea";
    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl service;

    @Test
    @DisplayName("T-SEC-005 / FR-SEC-AUTH-002: valid credentials return a Bearer token with the configured expiry")
    void login_validCredentials_returnsBearerToken() {
        // ARRANGE
        SecurityPrincipal principal =
                new SecurityPrincipal(42L, "andrea", "andrea@email.com", null, true, Set.of(Role.USER));
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(
                        principal, null, principal.getAuthorities()));
        when(jwtService.issueAccessToken(42L)).thenReturn("signed.jwt.token");
        when(jwtService.expiresInSeconds()).thenReturn(900L);

        // ACT
        AuthResponse result = service.login(new LoginRequest(IDENTIFIER, PASSWORD));

        // ASSERT
        assertThat(result).isEqualTo(new AuthResponse("signed.jwt.token", "Bearer", 900L));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThat(captor.getValue().getPrincipal()).isEqualTo(IDENTIFIER);
        assertThat(captor.getValue().getCredentials()).isEqualTo(PASSWORD);
        assertThat(captor.getValue().isAuthenticated()).isFalse();
    }

    @ParameterizedTest
    @MethodSource("authenticationFailures")
    @DisplayName("T-SEC-006 / FR-SEC-AUTH-003/004: any authentication failure yields the same generic error and no token")
    void login_authenticationFailure_throwsGenericBadCredentialsAndNeverIssuesToken(
            AuthenticationException failure) {
        // ARRANGE
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(failure);

        // ACT
        Throwable thrown = catchThrowable(() -> service.login(new LoginRequest(IDENTIFIER, PASSWORD)));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
        verifyNoInteractions(jwtService);
    }

    @Test
    void login_internalAuthenticationFailure_isNotMaskedAsBadCredentials() {
        // ARRANGE
        InternalAuthenticationServiceException failure =
                new InternalAuthenticationServiceException("database unavailable");
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(failure);

        // ACT
        Throwable thrown = catchThrowable(() -> service.login(new LoginRequest(IDENTIFIER, PASSWORD)));

        // ASSERT
        assertThat(thrown).isSameAs(failure);
        verifyNoInteractions(jwtService);
    }

    @Test
    void currentUser_mapsPrincipalToMinimalProfileWithPlainRoleNames() {
        // ARRANGE
        SecurityPrincipal principal = new SecurityPrincipal(
                42L, "andrea", "andrea@email.com", null, true, Set.of(Role.USER, Role.STAFF));

        // ACT
        AuthenticatedUserResponse result = service.currentUser(principal);

        // ASSERT
        assertThat(result).isEqualTo(
                new AuthenticatedUserResponse(42L, "andrea", "andrea@email.com", List.of("STAFF", "USER")));
    }

    static Stream<AuthenticationException> authenticationFailures() {
        return Stream.of(
                new BadCredentialsException("Bad credentials"),
                new DisabledException("User is disabled"),
                new LockedException("User account is locked"),
                new UsernameNotFoundException("User not found")
        );
    }
}