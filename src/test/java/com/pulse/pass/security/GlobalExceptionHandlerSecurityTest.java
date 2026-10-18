package com.pulse.pass.security;

import com.pulse.pass.dto.response.ErrorResponse;
import com.pulse.pass.exception.AuthenticationRequiredException;
import com.pulse.pass.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerSecurityTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("T-SEC-008: invalid token maps to a 401 ErrorResponse")
    void handleInvalidJwt_returns401ErrorResponse() {
        assertError(handler.handleInvalidJwt(new InvalidJwtException()),
                401, "Unauthorized", "Invalid or expired token");
    }

    @Test
    @DisplayName("T-SEC-007: missing authentication maps to a 401 ErrorResponse")
    void handleAuthenticationRequired_returns401ErrorResponse() {
        assertError(handler.handleAuthenticationRequired(new AuthenticationRequiredException()),
                401, "Unauthorized", "Authentication is required");
    }

    @Test
    @DisplayName("T-SEC-019: access denied maps to a 403 ErrorResponse without the internal reason")
    void handleAccessDenied_returns403ErrorResponseWithoutInternalReason() {
        ResponseEntity<ErrorResponse> result =
                handler.handleAccessDenied(new AccessDeniedException("internal reason"));

        assertError(result, 403, "Forbidden", "Access denied");
        assertThat(result.getBody().message()).doesNotContain("internal reason");
    }

    private void assertError(ResponseEntity<ErrorResponse> result, int status, String error, String message) {
        assertThat(result.getStatusCode().value()).isEqualTo(status);

        ErrorResponse body = result.getBody();
        assertThat(body).isNotNull();
        assertThat(body.timestamp()).isNotNull();
        assertThat(body.status()).isEqualTo(status);
        assertThat(body.error()).isEqualTo(error);
        assertThat(body.message()).isEqualTo(message);
        assertThat(body.details()).isEmpty();
    }
}