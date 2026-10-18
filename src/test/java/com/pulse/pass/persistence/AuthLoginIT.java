package com.pulse.pass.persistence;

import com.jayway.jsonpath.JsonPath;
import com.pulse.pass.domain.User;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthLoginIT {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldLoginWithUsernameAndReturnTokenForThatUser() throws Exception {
        register("login.user", "login.user@email.com");

        String body = mockMvc.perform(login("login.user", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(body, "$.accessToken");
        Long userId = userRepository.findByUsername("login.user").orElseThrow().getId();

        assertThat(jwtService.validateAndExtractUserId(token)).isEqualTo(userId);
        assertThat(body).doesNotContain(PASSWORD).doesNotContain("$2");
    }

    @Test
    void shouldLoginWithEmailIgnoringCase() throws Exception {
        register("login.mail", "Login.Mail@Email.com");

        mockMvc.perform(login("login.mail@email.com", PASSWORD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void shouldRejectWrongPasswordWithGenericMessage() throws Exception {
        register("login.wrong", "login.wrong@email.com");

        mockMvc.perform(login("login.wrong", "a-different-password-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldRejectUnknownUserWithTheSameMessage() throws Exception {
        mockMvc.perform(login("nobody.here", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void shouldRejectInactiveUserEvenWithCorrectPasswordUsingTheSameMessage() throws Exception {
        register("login.inactive", "login.inactive@email.com");
        User user = userRepository.findByUsername("login.inactive").orElseThrow();
        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(login("login.inactive", PASSWORD))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    private void register(String username, String email) {
        userService.register(new RegisterUserRequest(
                username, email, PASSWORD, "Login", "Test",
                "3001234567", "Santa Marta", LocalDate.of(1995, 4, 12)));
    }

    private MockHttpServletRequestBuilder login(String identifier, String password) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"identifier": "%s", "password": "%s"}
                        """.formatted(identifier, password));
    }
}