package com.pulse.pass.controller;

import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.dto.request.CreateEventRequest;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.dto.response.EventSummaryResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(EventController.class)
@Import(GlobalExceptionHandler.class)
class EventControllerTest {

    private static final String EVENT_CODE = "CMF-2026";

    private static final String VALID_BODY = """
            {
              "eventCode": "CMF-2026",
              "name": "Caribbean Music Fest 2026",
              "description": "Festival de musica del Caribe",
              "category": "MUSIC",
              "eventDate": "2026-12-01T20:00:00",
              "minimumAge": 18,
              "venueCode": "VEN-SMR-01"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService service;

    // ------------------------------------------------------------------
    // POST /api/events
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-001: valid create returns 201")
    void shouldCreateEvent() throws Exception {
        // ARRANGE
        when(service.create(any(CreateEventRequest.class)))
                .thenReturn(eventResponse(EventStatus.DRAFT, List.of()));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.category").value("MUSIC"))
                .andExpect(jsonPath("$.minimumAge").value(18))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.venueName").value("Marina Convention Center"))
                .andExpect(jsonPath("$.artists").isArray());

        ArgumentCaptor<CreateEventRequest> captor = ArgumentCaptor.forClass(CreateEventRequest.class);
        verify(service).create(captor.capture());
        CreateEventRequest sent = captor.getValue();
        assertThat(sent.eventCode()).isEqualTo("CMF-2026");
        assertThat(sent.name()).isEqualTo("Caribbean Music Fest 2026");
        assertThat(sent.category()).isEqualTo(EventCategory.MUSIC);
        assertThat(sent.eventDate()).isEqualTo(LocalDateTime.of(2026, 12, 1, 20, 0));
        assertThat(sent.minimumAge()).isEqualTo(18);
        assertThat(sent.venueCode()).isEqualTo("VEN-SMR-01");
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-002: invalid request returns 400 with details and never calls Service")
    void shouldReturn400WhenCreateRequestIsInvalid() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventCode": "",
                                  "name": "",
                                  "category": null,
                                  "eventDate": null,
                                  "minimumAge": -1,
                                  "venueCode": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.name").value("Event name is required"))
                .andExpect(jsonPath("$.details.category").value("Category is required"))
                .andExpect(jsonPath("$.details.eventDate").value("Event date is required"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age cannot be negative"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"));

        verify(service, never()).create(any(CreateEventRequest.class));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private EventResponse eventResponse(EventStatus status, List<String> artists) {
        return new EventResponse(
                1L,
                EVENT_CODE,
                "Caribbean Music Fest 2026",
                "Festival de musica del Caribe",
                EventCategory.MUSIC,
                status,
                LocalDateTime.of(2026, 12, 1, 20, 0),
                18,
                "VEN-SMR-01",
                "Marina Convention Center",
                artists);
    }

    private EventSummaryResponse eventSummary() {
        return new EventSummaryResponse(
                1L,
                EVENT_CODE,
                "Caribbean Music Fest 2026",
                EventCategory.MUSIC,
                EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 1, 20, 0),
                "Marina Convention Center");
    }
}
