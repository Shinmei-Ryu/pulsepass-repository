package com.pulse.pass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String ISSUER = "pulsepass-api";
    private static final Instant NOW = Instant.parse("2026-10-12T10:00:00Z");
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final String SECRET_A = secret(64, 1);
    private static final String SECRET_B = secret(64, 100);

    private final JwtService service = service(SECRET_A, ISSUER, NOW);

    // ------------------------------------------------------------------
    // issueAccessToken
    // ------------------------------------------------------------------

    @Test
    void issueAccessToken_producesSignedTokenWithExpectedClaims() {
        // ACT
        String token = service.issueAccessToken(42L);

        // ASSERT
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(keyA())
                .clock(() -> Date.from(NOW))
                .build()
                .parseSignedClaims(token);
        Claims claims = jws.getPayload();

        assertThat(jws.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getIssuedAt().toInstant()).isEqualTo(NOW);
        assertThat(claims.getExpiration().toInstant()).isEqualTo(NOW.plus(TTL));
        assertThat(claims.getId()).isNotBlank();
        // sin roles ni datos personales: solo identificador y metadatos
        assertThat(claims.keySet()).containsExactlyInAnyOrder("sub", "iss", "iat", "exp", "jti");
    }

    @Test
    void issueAccessToken_generatesDifferentTokenIdEachTime() {
        // ACT
        String first = service.issueAccessToken(42L);
        String second = service.issueAccessToken(42L);

        // ASSERT
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void issueAccessToken_nullUserId_throws() {
        assertThatThrownBy(() -> service.issueAccessToken(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void expiresInSeconds_returnsConfiguredTtl() {
        assertThat(service.expiresInSeconds()).isEqualTo(900L);
    }

    // ------------------------------------------------------------------
    // validateAndExtractUserId — token válido
    // ------------------------------------------------------------------

    @Test
    void validate_validToken_returnsUserId() {
        // ARRANGE
        String token = service.issueAccessToken(42L);

        // ACT + ASSERT
        assertThat(service.validateAndExtractUserId(token)).isEqualTo(42L);
    }

    @Test
    void validate_tokenJustBeforeExpiration_isAccepted() {
        // ARRANGE
        String token = service.issueAccessToken(42L);
        JwtService almostExpired = service(SECRET_A, ISSUER, NOW.plusSeconds(14 * 60 + 59));

        // ACT + ASSERT
        assertThat(almostExpired.validateAndExtractUserId(token)).isEqualTo(42L);
    }

    // ------------------------------------------------------------------
    // validateAndExtractUserId — rechazos
    // ------------------------------------------------------------------

    @Test
    void validate_expiredToken_isRejected() {
        // ARRANGE
        String token = service.issueAccessToken(42L);
        JwtService later = service(SECRET_A, ISSUER, NOW.plus(Duration.ofMinutes(16)));

        // ACT + ASSERT
        assertThatThrownBy(() -> later.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class)
                .hasMessage("Invalid or expired token");
    }

    @Test
    void validate_tokenFromAnotherIssuer_isRejected() {
        // ARRANGE
        String token = service(SECRET_A, "other-issuer", NOW).issueAccessToken(42L);

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_tokenSignedWithAnotherKey_isRejected() {
        // ARRANGE
        String token = service(SECRET_B, ISSUER, NOW).issueAccessToken(42L);

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_tamperedPayload_isRejected() {
        // ARRANGE: payload del usuario 1 con la firma del usuario 42
        String[] userOne = service.issueAccessToken(1L).split("\\.");
        String[] userFortyTwo = service.issueAccessToken(42L).split("\\.");
        String tampered = userOne[0] + "." + userOne[1] + "." + userFortyTwo[2];

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(tampered))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_unsignedToken_isRejected() {
        // ARRANGE: alg=none, sin firma
        String unsigned = Jwts.builder()
                .subject("42")
                .issuer(ISSUER)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(NOW.plus(TTL)))
                .id(UUID.randomUUID().toString())
                .compact();

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(unsigned))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_tokenSignedWithUnexpectedAlgorithm_isRejected() {
        // ARRANGE: misma clave, pero HS512 en lugar de HS256
        String hs512 = Jwts.builder()
                .subject("42")
                .issuer(ISSUER)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(NOW.plus(TTL)))
                .id(UUID.randomUUID().toString())
                .signWith(keyA(), Jwts.SIG.HS512)
                .compact();

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(hs512))
                .isInstanceOf(InvalidJwtException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "not-a-jwt", "a.b.c", "a.b"})
    void validate_malformedOrEmptyToken_isRejected(String token) {
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sub", "iat", "exp", "jti"})
    void validate_tokenMissingRequiredClaim_isRejected(String missingClaim) {
        // ARRANGE
        String token = tokenMissing(missingClaim);

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_nonNumericSubject_isRejected() {
        // ARRANGE
        String token = signed(baseClaims().subject("abc"));

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_tokenIssuedFarInTheFuture_isRejected() {
        // ARRANGE
        String token = signed(baseClaims().issuedAt(Date.from(NOW.plus(Duration.ofMinutes(5)))));

        // ACT + ASSERT
        assertThatThrownBy(() -> service.validateAndExtractUserId(token))
                .isInstanceOf(InvalidJwtException.class);
    }

    @Test
    void validate_tokenIssuedWithinClockSkew_isAccepted() {
        // ARRANGE
        String token = signed(baseClaims().issuedAt(Date.from(NOW.plusSeconds(30))));

        // ACT + ASSERT
        assertThat(service.validateAndExtractUserId(token)).isEqualTo(42L);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String secret(int length, int seed) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (seed + i * 7);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static JwtService service(String secret, String issuer, Instant instant) {
        return new JwtService(
                new JwtProperties(issuer, secret, TTL),
                Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static SecretKey keyA() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET_A));
    }

    private static JwtBuilder baseClaims() {
        return Jwts.builder()
                .subject("42")
                .issuer(ISSUER)
                .issuedAt(Date.from(NOW))
                .expiration(Date.from(NOW.plus(TTL)))
                .id(UUID.randomUUID().toString());
    }

    private static String signed(JwtBuilder builder) {
        return builder.signWith(keyA(), Jwts.SIG.HS256).compact();
    }

    private static String tokenMissing(String missingClaim) {
        JwtBuilder builder = Jwts.builder().issuer(ISSUER);

        if (!"sub".equals(missingClaim)) {
            builder.subject("42");
        }
        if (!"iat".equals(missingClaim)) {
            builder.issuedAt(Date.from(NOW));
        }
        if (!"exp".equals(missingClaim)) {
            builder.expiration(Date.from(NOW.plus(TTL)));
        }
        if (!"jti".equals(missingClaim)) {
            builder.id(UUID.randomUUID().toString());
        }

        return signed(builder);
    }
}