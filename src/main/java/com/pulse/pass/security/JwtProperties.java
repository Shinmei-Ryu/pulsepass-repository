package com.pulse.pass.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Base64;

@ConfigurationProperties(prefix = "pulsepass.security.jwt")
public record JwtProperties(
        @DefaultValue("pulsepass-api") String issuer,
        String secretBase64,
        @DefaultValue("PT15M") Duration accessTokenTtl
) {

    private static final int MIN_SECRET_BYTES = 32;

    public JwtProperties {
        if (secretBase64 == null || secretBase64.isBlank()) {
            throw new IllegalStateException(
                    "JWT secret is missing: set PULSEPASS_JWT_SECRET_BASE64");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secretBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT secret must be valid Base64");
        }

        if (decoded.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must decode to at least " + MIN_SECRET_BYTES + " bytes");
        }
    }

    // El toString generado por el record imprimiría el secreto
    @Override
    public String toString() {
        return "JwtProperties[issuer=" + issuer
                + ", accessTokenTtl=" + accessTokenTtl
                + ", secretBase64=***]";
    }
}