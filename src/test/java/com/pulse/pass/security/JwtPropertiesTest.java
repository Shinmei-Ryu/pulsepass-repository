package com.pulse.pass.security;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtPropertiesTest {

    private static final String VALID_SECRET = base64OfBytes(32);

    @Test
    void shouldAcceptSecretWithExactlyMinimumLength() {
        JwtProperties properties =
                new JwtProperties("pulsepass-api", VALID_SECRET, Duration.ofMinutes(15));

        assertThat(properties.issuer()).isEqualTo("pulsepass-api");
        assertThat(properties.secretBase64()).isEqualTo(VALID_SECRET);
        assertThat(properties.accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
    }

    @Test
    void shouldRejectMissingSecret() {
        assertThatThrownBy(() -> new JwtProperties("pulsepass-api", null, Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PULSEPASS_JWT_SECRET_BASE64");
    }

    @Test
    void shouldRejectBlankSecret() {
        assertThatThrownBy(() -> new JwtProperties("pulsepass-api", "   ", Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PULSEPASS_JWT_SECRET_BASE64");
    }

    @Test
    void shouldRejectSecretThatIsNotBase64() {
        assertThatThrownBy(() -> new JwtProperties("pulsepass-api", "not base64 !!!", Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64");
    }

    @Test
    void shouldRejectSecretShorterThanMinimum() {
        assertThatThrownBy(() -> new JwtProperties("pulsepass-api", base64OfBytes(31), Duration.ofMinutes(15)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 bytes");
    }

    @Test
    void shouldNotExposeSecretInToString() {
        JwtProperties properties =
                new JwtProperties("pulsepass-api", VALID_SECRET, Duration.ofMinutes(15));

        assertThat(properties.toString()).doesNotContain(VALID_SECRET);
    }

    private static String base64OfBytes(int length) {
        return Base64.getEncoder().encodeToString(new byte[length]);
    }
}