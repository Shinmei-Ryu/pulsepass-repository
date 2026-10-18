package com.pulse.pass.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {

    // El toString generado por el record imprimiría el token
    @Override
    public String toString() {
        return "AuthResponse[accessToken=***, tokenType=" + tokenType + ", expiresIn=" + expiresIn + "]";
    }
}