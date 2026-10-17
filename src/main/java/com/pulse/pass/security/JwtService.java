package com.pulse.pass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Component
public class JwtService {

    private static final String EXPECTED_ALGORITHM = "HS256";
    private static final Duration MAX_CLOCK_SKEW = Duration.ofSeconds(60);

    private final SecretKey key;
    private final String issuer;
    private final Duration accessTokenTtl;
    private final Clock clock;

    @Autowired
    public JwtService(JwtProperties properties) {
        this(properties, Clock.systemUTC());
    }

    // Visible para tests: permite fijar el reloj
    JwtService(JwtProperties properties, Clock clock) {
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.secretBase64()));
        this.issuer = properties.issuer();
        this.accessTokenTtl = properties.accessTokenTtl();
        this.clock = clock;
    }

    // Solo identificador y metadatos: roles y estado se consultan en cada request
    public String issueAccessToken(Long userId) {
        Objects.requireNonNull(userId, "userId is required");

        Instant now = clock.instant();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public long expiresInSeconds() {
        return accessTokenTtl.toSeconds();
    }

    // Verifica firma, algoritmo, issuer, expiración y claims obligatorios; devuelve el id del usuario
    public Long validateAndExtractUserId(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidJwtException();
        }

        Jws<Claims> jws;
        try {
            jws = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clock(() -> Date.from(clock.instant()))
                    .build()
                    .parseSignedClaims(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidJwtException();
        }

        // JJWT acepta HS384/HS512 con la misma clave: el contrato exige HS256
        if (!EXPECTED_ALGORITHM.equals(jws.getHeader().getAlgorithm())) {
            throw new InvalidJwtException();
        }

        Claims claims = jws.getPayload();

        if (isBlank(claims.getSubject()) || isBlank(claims.getId())
                || claims.getExpiration() == null || claims.getIssuedAt() == null) {
            throw new InvalidJwtException();
        }

        // iat inaceptable: emitido en el futuro (más allá de la tolerancia)
        if (claims.getIssuedAt().toInstant().isAfter(clock.instant().plus(MAX_CLOCK_SKEW))) {
            throw new InvalidJwtException();
        }

        try {
            return Long.valueOf(claims.getSubject());
        } catch (NumberFormatException ex) {
            throw new InvalidJwtException();
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}