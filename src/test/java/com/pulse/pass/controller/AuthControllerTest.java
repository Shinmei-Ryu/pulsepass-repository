package com.pulse.pass.controller;

import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.security.SecurityConfig;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}