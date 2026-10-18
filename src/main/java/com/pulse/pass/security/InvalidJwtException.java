package com.pulse.pass.security;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtException extends AuthenticationException {

    // Mensaje único: no distingue expirado, manipulado o malformado
    public InvalidJwtException() {
        super("Invalid or expired token");
    }
}