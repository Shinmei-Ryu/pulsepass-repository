package com.pulse.pass.persistence;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import org.junit.jupiter.api.Test;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
class SecurityMigrationIT {

    @Container
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Test
    void shouldUpgradeFromV3WithExistingDataToV4() throws SQLException {

        flyway("3").migrate();

        execute("INSERT INTO users (username, email, active) VALUES ('legacy', 'legacy@email.com', TRUE)");

        Flyway latest = flyway(null);
        latest.migrate();

        assertThat(latest.info().current().getVersion().getVersion()).isEqualTo("4");
        assertThat(count("SELECT COUNT(*) FROM users WHERE username = 'legacy'")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM user_credentials")).isZero();
        assertThat(count("SELECT COUNT(*) FROM user_roles")).isZero();
    }

    private Flyway flyway(String target) {
        FluentConfiguration configuration = Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration");

        if (target != null) {
            configuration.target(target);
        }

        return configuration.load();
    }

    private void execute(String sql) throws SQLException {
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private long count(String sql) throws SQLException {
        try (Connection connection = connection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            resultSet.next();
            return resultSet.getLong(1);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
    }
}