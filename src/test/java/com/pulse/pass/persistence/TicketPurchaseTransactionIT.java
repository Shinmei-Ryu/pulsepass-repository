package com.pulse.pass.persistence;

import com.jayway.jsonpath.JsonPath;
import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.TicketStatus;
import com.pulse.pass.domain.Venue;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regresión de DEF-001. A propósito esta clase NO lleva @Transactional: así la única transacción
 * es la del Service, igual que en producción. Con @Transactional de clase, la transacción del test
 * envolvía a purchase() y ocultaba que el método heredaba readOnly = true.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class TicketPurchaseTransactionIT {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";
    private static final String EVENT_CODE = "DEF001-EVT";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired private MockMvc mockMvc;
    @Autowired private VenueRepository venueRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private TicketRepository ticketRepository;

    @Test
    @DisplayName("DEF-001: purchase persists the ticket when no outer transaction wraps the service")
    void purchaseCommitsWithoutOuterTransaction() throws Exception {
        Venue venue = venueRepository.saveAndFlush(
                new Venue("VEN-DEF001", "Venue DEF-001", "Santa Marta", "Calle 1 #1-01", 50, true));
        Event event = new Event(EVENT_CODE, "Event DEF-001", "Regresion", EventCategory.MUSIC,
                EventStatus.PUBLISHED, LocalDateTime.now().plusDays(30), 18);
        event.setVenue(venue);
        eventRepository.saveAndFlush(event);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "def001", "email": "def001@email.com", "password": "%s",
                                 "firstName": "Demo", "lastName": "Account", "birthDate": "1995-04-12"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isCreated());

        String loginBody = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier": "def001", "password": "%s"}
                                """.formatted(PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(loginBody, "$.accessToken");

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userEmail": "def001@email.com", "eventCode": "%s", "type": "GENERAL"}
                                """.formatted(EVENT_CODE)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"));

        // Se consulta fuera de cualquier transacción del test: solo hay fila si el Service confirmó
        assertThat(ticketRepository.countByEventEventCodeAndStatus(EVENT_CODE, TicketStatus.PAID))
                .isEqualTo(1);
    }
}
