package com.pulse.pass.persistence;

import com.jayway.jsonpath.JsonPath;
import com.pulse.pass.domain.User;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.service.UserService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BearerAuthenticationIT {

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

    @Test
    void shouldReachMeWithTokenIssuedByLogin() throws Exception {
        String token = registerAndLogin("bearer.ok");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bearer.ok"))
                .andExpect(jsonPath("$.email").value("bearer.ok@email.com"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void shouldRejectTokenOfUserDeactivatedAfterLogin() throws Exception {
        String token = registerAndLogin("bearer.inactive");

        User user = userRepository.findByUsername("bearer.inactive").orElseThrow();
        user.setActive(false);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void shouldRejectTamperedToken() throws Exception {
        String[] userA = registerAndLogin("bearer.a").split("\\.");
        String[] userB = registerAndLogin("bearer.b").split("\\.");

        // cabecera y firma de A con el payload de B
        String tampered = userA[0] + "." + userB[1] + "." + userA[2];

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required"));
    }

    @Test
    void shouldNotAcceptTokenFromQueryString() throws Exception {
        String token = registerAndLogin("bearer.query");

        mockMvc.perform(get("/api/auth/me").param("access_token", token))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndLogin(String username) throws Exception {
        userService.register(new RegisterUserRequest(
                username, username + "@email.com", PASSWORD, "Bearer", "Test",
                "3001234567", "Santa Marta", LocalDate.of(1995, 4, 12)));

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier": "%s", "password": "%s"}
                                """.formatted(username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return JsonPath.read(body, "$.accessToken");
    }
}