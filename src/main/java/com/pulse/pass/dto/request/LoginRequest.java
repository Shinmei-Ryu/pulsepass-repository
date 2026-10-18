package com.pulse.pass.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Identifier is required")
        String identifier,
        @NotBlank(message = "Password is required")
        String password
) {

    // El toString generado por el record imprimiría la contraseña
    @Override
    public String toString() {
        return "LoginRequest[identifier=" + identifier + ", password=***]";
    }
}