package com.pulse.pass.persistence;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.repository.UserRoleRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@Transactional
class SecurityPersistenceIT {

    private static final String FAKE_HASH = "fake-hash-for-tests-only";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialRepository credentialRepository;

    @Autowired
    private UserRoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistCredentialForUser() {

        User user = userRepository.save(new User("andrea", "andrea@email.com", true));

        credentialRepository.save(new UserCredential(user, FAKE_HASH));
        entityManager.flush();
        entityManager.clear();

        UserCredential retrieved = credentialRepository.findById(user.getId()).orElseThrow();

        assertThat(retrieved.getUserId()).isEqualTo(user.getId());
        assertThat(retrieved.getPasswordHash()).isEqualTo(FAKE_HASH);
        assertThat(retrieved.getPasswordChangedAt()).isNotNull();
    }

    @Test
    void shouldPersistMultipleRolesForUser() {

        User user = userRepository.save(new User("laura", "laura@email.com", true));

        roleRepository.save(new UserRole(user, Role.USER));
        roleRepository.save(new UserRole(user, Role.STAFF));
        entityManager.flush();
        entityManager.clear();

        List<UserRole> roles = roleRepository.findByUser_Id(user.getId());

        assertThat(roles)
                .extracting(UserRole::getRole)
                .containsExactlyInAnyOrder(Role.USER, Role.STAFF);
    }

    @Test
    void shouldRejectDuplicateRoleAtDatabaseLevel() {

        User user = userRepository.save(new User("carlos", "carlos@email.com", true));

        insertRole(user.getId(), "USER");

        assertThrows(PersistenceException.class, () -> insertRole(user.getId(), "USER"));
    }

    @Test
    void shouldRejectUnknownRoleAtDatabaseLevel() {

        User user = userRepository.save(new User("miguel", "miguel@email.com", true));

        assertThrows(PersistenceException.class, () -> insertRole(user.getId(), "SUPERUSER"));
    }

    @Test
    void legacyUserWithoutCredentialHasNeitherCredentialNorRoles() {

        User user = userRepository.save(new User("legacy", "legacy@email.com", true));

        assertThat(credentialRepository.findById(user.getId())).isEmpty();
        assertThat(roleRepository.findByUser_Id(user.getId())).isEmpty();
    }

    private void insertRole(Long userId, String role) {
        entityManager
                .createNativeQuery("INSERT INTO user_roles (user_id, role) VALUES (:userId, :role)")
                .setParameter("userId", userId)
                .setParameter("role", role)
                .executeUpdate();
    }
}
