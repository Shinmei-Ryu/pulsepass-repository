package com.pulse.pass.controller;

import com.pulse.pass.domain.Role;
import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;
import com.pulse.pass.dto.response.AuthenticatedUserResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.security.*;
import com.pulse.pass.service.AuthService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerTest {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    private static final String VALID_BODY = """
            {
              "identifier": "andrea",
              "password": "UnaFraseSeguraLarga2026!"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("T-SEC-005: valid login returns 200 with Bearer token and expiry")
    void shouldLoginAndReturnBearerToken() throws Exception {
        // ARRANGE
        when(service.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("signed.jwt.token", "Bearer", 900L));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").value("signed.jwt.token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        ArgumentCaptor<LoginRequest> captor = ArgumentCaptor.forClass(LoginRequest.class);
        verify(service).login(captor.capture());
        assertThat(captor.getValue().identifier()).isEqualTo("andrea");
        assertThat(captor.getValue().password()).isEqualTo(PASSWORD);
    }

    @Test
    @DisplayName("T-SEC-006 / T-SEC-019: invalid credentials return a generic 401 without leaking data")
    void shouldReturn401WithGenericErrorWhenCredentialsAreInvalid() throws Exception {
        // ARRANGE
        when(service.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.details").isMap())
                // solo los 5 campos del contrato: ni trace, ni path, ni exception
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    @Test
    @DisplayName("T-SEC-019: unexpected error during login returns a generic 500 without internal details")
    void shouldReturnGeneric500WhenUnexpectedErrorOccurs() throws Exception {
        // ARRANGE
        when(service.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("internal failure details"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(content().string(not(containsString("internal failure details"))))
                .andExpect(content().string(not(containsString(PASSWORD))));
    }

    @Test
    void shouldReturn400WhenIdentifierAndPasswordAreBlank() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.identifier").value("Identifier is required"))
                .andExpect(jsonPath("$.details.password").value("Password is required"));

        verify(service, never()).login(any(LoginRequest.class));
    }

    @Test
    @DisplayName("GET /api/auth/me returns the minimal profile of the authenticated user")
    void shouldReturnCurrentUserWhenBearerTokenIsValid() throws Exception {
        // ARRANGE
        SecurityPrincipal principal = principal(true);
        when(jwtService.validateAndExtractUserId("good-token")).thenReturn(42L);
        when(userDetailsService.loadPrincipalById(42L)).thenReturn(principal);
        when(service.currentUser(any(SecurityPrincipal.class)))
                .thenReturn(new AuthenticatedUserResponse(42L, "andrea", "andrea@email.com", List.of("USER")));

        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer good-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(service).currentUser(principal);
    }

    @Test
    @DisplayName("T-SEC-007: protected route without token returns a JSON 401")
    void shouldReturn401WhenTokenIsMissing() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.length()").value(5));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("T-SEC-008: tampered or expired token returns a JSON 401")
    void shouldReturn401WhenTokenIsInvalid() throws Exception {
        // ARRANGE
        when(jwtService.validateAndExtractUserId("tampered-token")).thenThrow(new InvalidJwtException());

        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer tampered-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("T-SEC-016: valid token of an inactive user returns a JSON 401")
    void shouldReturn401WhenTokenBelongsToInactiveUser() throws Exception {
        // ARRANGE
        when(jwtService.validateAndExtractUserId("good-token")).thenReturn(42L);
        when(userDetailsService.loadPrincipalById(42L)).thenReturn(principal(false));

        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer good-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("FR-SEC-AUTH-007: a token sent in the query string is not accepted")
    void shouldReturn401WhenTokenIsOnlyInQueryString() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/auth/me").param("access_token", "good-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));

        verifyNoInteractions(jwtService, service);
    }

    private SecurityPrincipal principal(boolean active) {
        return new SecurityPrincipal(42L, "andrea", "andrea@email.com", null, active, Set.of(Role.USER));
    }
}