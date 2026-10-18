package com.pulse.pass.security;

import com.pulse.pass.exception.AuthenticationRequiredException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RestSecurityHandlersTest {

    @Mock
    private HandlerExceptionResolver resolver;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    void entryPoint_invalidToken_delegatesTheSameExceptionAndSetsChallengeHeader() {
        // ARRANGE
        InvalidJwtException exception = new InvalidJwtException();

        // ACT
        new RestAuthenticationEntryPoint(resolver).commence(request, response, exception);

        // ASSERT
        assertThat(response.getHeader("WWW-Authenticate")).isEqualTo("Bearer");
        verify(resolver).resolveException(request, response, null, exception);
    }

    @Test
    void entryPoint_anyOtherAuthenticationFailure_delegatesAuthenticationRequired() {
        // ACT
        new RestAuthenticationEntryPoint(resolver).commence(request, response,
                new InsufficientAuthenticationException("Full authentication is required to access this resource"));

        // ASSERT
        ArgumentCaptor<Exception> captor = ArgumentCaptor.forClass(Exception.class);
        verify(resolver).resolveException(eq(request), eq(response), isNull(), captor.capture());
        assertThat(captor.getValue()).isInstanceOf(AuthenticationRequiredException.class);
        assertThat(captor.getValue().getMessage()).doesNotContain("Full authentication");
    }

    @Test
    void accessDeniedHandler_delegatesTheSameException() {
        // ARRANGE
        AccessDeniedException exception = new AccessDeniedException("internal reason");

        // ACT
        new RestAccessDeniedHandler(resolver).handle(request, response, exception);

        // ASSERT
        verify(resolver).resolveException(request, response, null, exception);
    }
}