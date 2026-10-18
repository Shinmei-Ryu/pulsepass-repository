package com.pulse.pass.persistence;

import com.jayway.jsonpath.JsonPath;
import com.pulse.pass.domain.*;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.UserService;
import org.junit.jupiter.api.DisplayName;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OwnershipSecurityIT {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private TicketRepository ticketRepository;

    @Test
    @DisplayName("T-SEC-010: a USER cannot buy a ticket on behalf of another email")
    void purchaseForAnotherEmail_returns403() throws Exception {
        givenPublishedEvent("OWN-PUB-1");
        String tokenA = registerAndLogin("own.a1");
        registerAndLogin("own.b1");
        long ticketsBefore = ticketRepository.count();

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody("own.b1@email.com", "OWN-PUB-1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        assertThat(ticketRepository.count()).isEqualTo(ticketsBefore);
    }

    @Test
    @DisplayName("T-SEC-011: a USER can cancel their own ticket")
    void cancelOwnTicket_returns200() throws Exception {
        givenPublishedEvent("OWN-PUB-4");
        String token = registerAndLogin("own.a7");
        String ticketCode = buyTicket(token, "own.a7@email.com", "OWN-PUB-4");

        mockMvc.perform(patch("/api/tickets/{code}/cancel", ticketCode)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("T-SEC-009: a USER cannot read or cancel a ticket owned by someone else")
    void foreignTicket_readAndCancel_return403() throws Exception {
        givenPublishedEvent("OWN-PUB-2");
        String tokenA = registerAndLogin("own.a2");
        String tokenB = registerAndLogin("own.b2");
        String ticketCodeOfB = buyTicket(tokenB, "own.b2@email.com", "OWN-PUB-2");

        mockMvc.perform(get("/api/tickets/{code}", ticketCodeOfB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/tickets/{code}/cancel", ticketCodeOfB)
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tickets/{code}", ticketCodeOfB)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("FR-SEC-TKT-03: a USER cannot list tickets of another email")
    void foreignTicketList_returns403() throws Exception {
        String tokenA = registerAndLogin("own.a3");
        registerAndLogin("own.b3");

        mockMvc.perform(get("/api/tickets/by-user").param("email", "own.b3@email.com")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tickets/by-user").param("email", "own.a3@email.com")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("T-SEC-017: a DRAFT event answers 404 to a regular user")
    void draftEvent_asUser_returns404() throws Exception {
        givenEvent("OWN-DRAFT-1", EventStatus.DRAFT);
        String tokenA = registerAndLogin("own.a4");

        mockMvc.perform(get("/api/events/{code}", "OWN-DRAFT-1")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("T-SEC-017: a PUBLISHED event stays public")
    void publishedEvent_isPublic() throws Exception {
        givenPublishedEvent("OWN-PUB-3");

        mockMvc.perform(get("/api/events/{code}", "OWN-PUB-3"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("T-SEC-015: a USER cannot look up another user by email")
    void userByEmail_foreign_returns403_ownReturns200() throws Exception {
        String tokenA = registerAndLogin("own.a5");
        registerAndLogin("own.b5");

        mockMvc.perform(get("/api/users/by-email").param("email", "own.b5@email.com")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        mockMvc.perform(get("/api/users/by-email").param("email", "own.a5@email.com")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("T-SEC-015: a USER cannot look up another user by username")
    void userByUsername_foreign_returns403_ownReturns200() throws Exception {
        String tokenA = registerAndLogin("own.a6");
        registerAndLogin("own.b6");

        mockMvc.perform(get("/api/users/by-username").param("username", "own.b6")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        mockMvc.perform(get("/api/users/by-username").param("username", "own.a6")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk());
    }

    // ---------------- helpers ----------------

    private void givenPublishedEvent(String code) {
        givenEvent(code, EventStatus.PUBLISHED);
    }

    private void givenEvent(String code, EventStatus status) {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-" + code, "Venue " + code, "Santa Marta", "Calle 1 #1-01", 50, true));
        Event event = new Event(code, "Event " + code, "Descripcion", EventCategory.MUSIC,
                status, LocalDateTime.now().plusDays(30), 18);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);
    }

    private String registerAndLogin(String username) throws Exception {
        userService.register(new RegisterUserRequest(
                username, username + "@email.com", PASSWORD, "Own", "Test",
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

    private String buyTicket(String token, String email, String eventCode) throws Exception {
        String body = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(purchaseBody(email, eventCode)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.ticketCode");
    }

    private String purchaseBody(String email, String eventCode) {
        return """
                {"userEmail": "%s", "eventCode": "%s", "type": "GENERAL"}
                """.formatted(email, eventCode);
    }
}