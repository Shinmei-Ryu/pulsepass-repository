package com.pulse.pass.controller;

import com.pulse.pass.domain.TicketStatus;
import com.pulse.pass.domain.TicketType;
import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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
