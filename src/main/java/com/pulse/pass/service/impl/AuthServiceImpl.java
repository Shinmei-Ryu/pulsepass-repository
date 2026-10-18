package com.pulse.pass.service.impl;

import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;
import com.pulse.pass.dto.response.AuthenticatedUserResponse;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.security.SecurityPrincipal;
import com.pulse.pass.service.AuthService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // FR-SEC-AUTH-002 / FR-SEC-AUTH-003 / FR-SEC-AUTH-004
    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.identifier(), request.password()));
        } catch (InternalAuthenticationServiceException ex) {
            // Falla interna (p. ej. la base de datos): no es un problema de credenciales
            throw ex;
        } catch (AuthenticationException ex) {
            // Clave incorrecta, usuario inexistente o cuenta inactiva: el cliente ve siempre lo mismo
            log.warn("Login failed: {}", ex.getClass().getSimpleName());
            throw new BadCredentialsException("Invalid credentials");
        }

        // El token se emite solo después de autenticar
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();
        log.info("Login succeeded for userId={}", principal.getId());

        return new AuthResponse(
                jwtService.issueAccessToken(principal.getId()),
                TOKEN_TYPE,
                jwtService.expiresInSeconds()
        );
    }

    @Override
    public AuthenticatedUserResponse currentUser(SecurityPrincipal principal) {

        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();

        return new AuthenticatedUserResponse(
                principal.getId(), principal.getUsername(), principal.getEmail(), roles);
    }
}