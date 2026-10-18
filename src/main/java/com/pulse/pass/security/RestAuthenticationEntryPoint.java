package com.pulse.pass.security;

import com.pulse.pass.exception.AuthenticationRequiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public RestAuthenticationEntryPoint(HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {

        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");

        // Token inválido se conserva; cualquier otra causa se reduce a "autenticación requerida"
        AuthenticationException exception = authException instanceof InvalidJwtException
                ? authException
                : new AuthenticationRequiredException();

        // GlobalExceptionHandler construye el ErrorResponse, igual que en el resto de errores
        resolver.resolveException(request, response, null, exception);
    }
}