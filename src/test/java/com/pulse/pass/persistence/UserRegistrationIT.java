package com.pulse.pass.persistence;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserProfileRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.repository.UserRoleRepository;
import com.pulse.pass.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@Testcontainers
@SpringBootTest
class UserRegistrationIT {

    private static final String PASSWORD = "a-strong-password";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository profileRepository;

    @Autowired
    private UserCredentialRepository credentialRepository;

    @MockitoSpyBean
    private UserRoleRepository roleRepository;

    @Test
    void shouldRegisterUserWithHashedCredentialAndUserRole() {

        userService.register(request("atomic.ok", "atomic.ok@email.com"));

        User user = userRepository.findByUsername("atomic.ok").orElseThrow();
        UserCredential credential = credentialRepository.findById(user.getId()).orElseThrow();

        assertThat(credential.getPasswordHash()).isNotEqualTo(PASSWORD).startsWith("$2");
        assertThat(roleRepository.findByUser_Id(user.getId()))
                .extracting(UserRole::getRole)
                .containsExactly(Role.USER);
        assertThat(profileRepository.findByUser_Id(user.getId())).isPresent();
    }

    @Test
    void shouldRollbackEverythingWhenRoleCannotBeSaved() {

        long credentialsBefore = credentialRepository.count();
        long profilesBefore = profileRepository.count();

        doThrow(new SimulatedFailureException())
                .when(roleRepository).save(any(UserRole.class));

        assertThrows(SimulatedFailureException.class,
                () -> userService.register(request("atomic.fail", "atomic.fail@email.com")));

        assertThat(userRepository.findByUsername("atomic.fail")).isEmpty();
        assertThat(credentialRepository.count()).isEqualTo(credentialsBefore);
        assertThat(profileRepository.count()).isEqualTo(profilesBefore);
    }

    private RegisterUserRequest request(String username, String email) {
        return new RegisterUserRequest(
                username, email, PASSWORD, "Atomic", "Test",
                "3001234567", "Santa Marta", LocalDate.of(1995, 4, 12));
    }

    private static class SimulatedFailureException extends RuntimeException {

        SimulatedFailureException() {
            super("simulated failure");
        }
    }
}