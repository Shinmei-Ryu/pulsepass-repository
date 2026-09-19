package com.pulse.pass.persistence;

import com.pulse.pass.repository.ArtistRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class FlywayMigrationIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private ArtistRepository artistRepository;

    @Test
    void shouldApplyMigrationsAndValidateSchemaOnContextStartup() {
        assertThat(artistRepository).isNotNull();
    }

    @Test
    void shouldHaveInitialArtistCatalogFromV2Migration() {

        long count = artistRepository.count();

        assertThat(count).isEqualTo(5);
    }
}