package com.pulse.pass.controller;

import com.pulse.pass.domain.TicketStatus;
import com.pulse.pass.domain.TicketType;
import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.service.TicketService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(TicketController.class)
@Import(GlobalExceptionHandler.class)
class TicketControllerTest {

    private static final String USER_EMAIL = "andrea@email.com";
    private static final String EVENT_CODE = "CMF-2026";
    private static final String TICKET_CODE = "TCK-CMF-2026-1";

    private static final String VALID_BODY = """
            {
              "userEmail": "andrea@email.com",
              "eventCode": "CMF-2026",
              "type": "VIP"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService service;

    // ------------------------------------------------------------------
    // POST /api/tickets
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-001: valid purchase returns 201 and TicketResponse")
    void shouldPurchaseTicket() throws Exception {
        // ARRANGE
        when(service.purchase(any(PurchaseTicketRequest.class))).thenReturn(ticket(TicketStatus.PAID));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ticketCode").value("TCK-CMF-2026-1"))
                .andExpect(jsonPath("$.type").value("VIP"))
                .andExpect(jsonPath("$.price").value(240000.0))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.userEmail").value("andrea@email.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.eventName").value("Caribbean Music Fest 2026"));

        ArgumentCaptor<PurchaseTicketRequest> captor = ArgumentCaptor.forClass(PurchaseTicketRequest.class);
        verify(service).purchase(captor.capture());
        PurchaseTicketRequest sent = captor.getValue();
        assertThat(sent.userEmail()).isEqualTo(USER_EMAIL);
        assertThat(sent.eventCode()).isEqualTo(EVENT_CODE);
        assertThat(sent.type()).isEqualTo(TicketType.VIP);
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-002: invalid request returns 400 with details and never calls Service")
    void shouldReturn400WhenPurchaseRequestIsInvalid() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userEmail": "not-an-email",
                                  "eventCode": "",
                                  "type": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email must be a valid email address"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(service, never()).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    void shouldReturn400WhenTicketTypeEnumValueIsInvalid() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("VIP", "FLYING")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or invalid JSON request"))
                .andExpect(jsonPath("$.details.body").exists());

        verify(service, never()).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-003: missing user returns 404 with ErrorResponse")
    void shouldReturn404WhenUserDoesNotExist() throws Exception {
        // ARRANGE
        when(service.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new ResourceNotFoundException("User not found: andrea@email.com"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: andrea@email.com"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).purchase(any(PurchaseTicketRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-TKT-004 / AC-CTRL-010: business rule (minimum age) returns 409")
    void shouldReturn409WhenPurchaseViolatesBusinessRule() throws Exception {
        // ARRANGE
        when(service.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new BusinessRuleException("User does not meet minimum age for event: CMF-2026"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User does not meet minimum age for event: CMF-2026"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).purchase(any(PurchaseTicketRequest.class));
    }

    // ------------------------------------------------------------------
    // GET /api/tickets/{ticketCode}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-005: find ticket by code returns 200")
    void shouldReturnTicketByCode() throws Exception {
        // ARRANGE
        when(service.findByCode(TICKET_CODE)).thenReturn(ticket(TicketStatus.PAID));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", TICKET_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TCK-CMF-2026-1"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.userEmail").value("andrea@email.com"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(service).findByCode(TICKET_CODE);
    }

    @Test
    void shouldReturn404WhenTicketDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findByCode("TCK-999"))
                .thenThrow(new ResourceNotFoundException("Ticket not found: TCK-999"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/{ticketCode}", "TCK-999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Ticket not found: TCK-999"));

        verify(service).findByCode("TCK-999");
    }

    // ------------------------------------------------------------------
    // GET /api/tickets/by-user?email=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-006: tickets by user returns 200 and a list")
    void shouldReturnTicketsByUser() throws Exception {
        // ARRANGE
        when(service.findByUserEmail(USER_EMAIL)).thenReturn(List.of(
                ticket(TicketStatus.PAID),
                ticket(TicketStatus.USED)));

        // ACT + ASSERT
        mockMvc.perform(get("/api/tickets/by-user").param("email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userEmail").value("andrea@email.com"))
                .andExpect(jsonPath("$[0].status").value("PAID"))
                .andExpect(jsonPath("$[1].status").value("USED"));

        verify(service).findByUserEmail(USER_EMAIL);
        // "by-user" must not be captured as {ticketCode}
        verify(service, never()).findByCode(anyString());
    }

    // ------------------------------------------------------------------
    // GET /api/events/{eventCode}/tickets/paid
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-TKT-007: PAID tickets by event returns 200 and a list")
    void shouldReturnPaidTicketsByEvent() throws Exception {
        // ARRANGE
        when(service.findPaidTicketsByEvent(EVENT_CODE)).thenReturn(List.of(ticket(TicketStatus.PAID)));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", EVENT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(service).findPaidTicketsByEvent(EVENT_CODE);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(
                1L,
                TICKET_CODE,
                TicketType.VIP,
                new BigDecimal("240000.00"),
                status,
                LocalDateTime.of(2026, 10, 10, 12, 0),
                USER_EMAIL,
                EVENT_CODE,
                "Caribbean Music Fest 2026");
    }
}
