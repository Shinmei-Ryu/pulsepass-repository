package com.pulse.pass.persistence;

import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserProfile;
import com.pulse.pass.repository.UserProfileRepository;
import com.pulse.pass.repository.UserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@Transactional
class UserProfileIT {

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
    private UserProfileRepository userProfileRepository;

    // QT-004 / FR-USR-003 / FR-USR-004
    @Test
    void shouldPersistUserWithProfile() {

        User user = new User("andrea.gomez", "andrea.gomez@pulsepass.com", true);
        userRepository.save(user);

        UserProfile profile = new UserProfile(
                "Andrea", "Gómez", "3001234567", "Santa Marta", LocalDate.of(1995, 4, 12)
        );
        profile.setUser(user);
        userProfileRepository.save(profile);

        User retrievedUser = userRepository.findByUsername("andrea.gomez").orElseThrow();
        UserProfile retrievedProfile =
                userProfileRepository.findByUser_Id(retrievedUser.getId()).orElseThrow();

        assertThat(retrievedProfile.getFirstName()).isEqualTo("Andrea");
        assertThat(retrievedProfile.getUser().getUsername()).isEqualTo("andrea.gomez");
    }

    // AC-004
    @Test
    void shouldViolateUniqueConstraintWhenAssigningSecondProfileToSameUser() {

        User user = new User("carlos.perez", "carlos.perez@pulsepass.com", true);
        userRepository.save(user);

        UserProfile firstProfile = new UserProfile(
                "Carlos", "Pérez", "3009876543", "Barranquilla", LocalDate.of(1990, 1, 20)
        );
        firstProfile.setUser(user);
        userProfileRepository.saveAndFlush(firstProfile);

        UserProfile secondProfile = new UserProfile(
                "Carlos", "Pérez Duplicado", "3009876544", "Barranquilla", LocalDate.of(1990, 1, 20)
        );
        secondProfile.setUser(user);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userProfileRepository.saveAndFlush(secondProfile)
        );
    }
}
