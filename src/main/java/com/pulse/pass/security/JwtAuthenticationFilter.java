package com.pulse.pass.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService userDetailsService,
                                   AuthenticationEntryPoint authenticationEntryPoint) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // No pisar una autenticación ya establecida
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Solo el encabezado Authorization: el JWT nunca se lee del query string
        Enumeration<String> headers = request.getHeaders(HttpHeaders.AUTHORIZATION);
        List<String> authorizationHeaders = headers == null ? List.of() : Collections.list(headers);

        if (authorizationHeaders.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Más de un Authorization es ambiguo y sospechoso
        if (authorizationHeaders.size() > 1) {
            reject(request, response, new InvalidJwtException());
            return;
        }

        String header = authorizationHeaders.get(0);

        // Otro esquema (por ejemplo Basic): no es asunto de este filtro
        if (!header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityPrincipal principal;
        try {
            principal = resolvePrincipal(header.substring(BEARER_PREFIX.length()).trim());
        } catch (InvalidJwtException ex) {
            reject(request, response, ex);
            return;
        }

        UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken
                .authenticated(principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(request, response);
    }

    private SecurityPrincipal resolvePrincipal(String token) {

        if (token.isEmpty()) {
            throw new InvalidJwtException();
        }

        Long userId = jwtService.validateAndExtractUserId(token);

        SecurityPrincipal principal;
        try {
            principal = userDetailsService.loadPrincipalById(userId);
        } catch (UsernameNotFoundException ex) {
            // El usuario o su credencial ya no existen
            throw new InvalidJwtException();
        }

        // Un token válido de una cuenta inactiva no da acceso (FR-SEC-AUTH-004)
        if (!principal.isEnabled()) {
            throw new InvalidJwtException();
        }

        return principal;
    }

    private void reject(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException ex) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        authenticationEntryPoint.commence(request, response, ex);
    }
}