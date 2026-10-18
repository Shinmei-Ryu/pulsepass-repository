package com.pulse.pass.controller;

import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.dto.request.CreateEventRequest;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.dto.response.EventSummaryResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.DuplicateResourceException;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.security.CustomUserDetailsService;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.security.SecurityConfig;
import com.pulse.pass.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(EventController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
@WithMockUser(roles = "ADMIN")
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

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

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

    @Test
    void shouldReturn400WhenMinimumAgeIsMissing() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventCode": "CMF-2026",
                                  "name": "Caribbean Music Fest 2026",
                                  "category": "MUSIC",
                                  "eventDate": "2026-12-01T20:00:00",
                                  "venueCode": "VEN-SMR-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age is required"));

        verify(service, never()).create(any(CreateEventRequest.class));
    }

    @Test
    void shouldReturn400WhenCategoryEnumValueIsInvalid() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("MUSIC", "FLYING")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or invalid JSON request"))
                .andExpect(jsonPath("$.details.body").exists());

        verify(service, never()).create(any(CreateEventRequest.class));
    }

    @Test
    @DisplayName("BR-EVENT-001: duplicated event code returns 409")
    void shouldReturn409WhenEventCodeAlreadyExists() throws Exception {
        // ARRANGE
        when(service.create(any(CreateEventRequest.class)))
                .thenThrow(new DuplicateResourceException("Event code already exists: CMF-2026"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Event code already exists: CMF-2026"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).create(any(CreateEventRequest.class));
    }

    // ------------------------------------------------------------------
    // GET /api/events/{eventCode}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-003: existing event returns 200")
    void shouldReturnEventByCode() throws Exception {
        // ARRANGE
        when(service.findByCode(EVENT_CODE))
                .thenReturn(eventResponse(EventStatus.PUBLISHED, List.of("Solar Beat")));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}", EVENT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.artists[0]").value("Solar Beat"));

        verify(service).findByCode(EVENT_CODE);
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-004: missing event returns 404 with ErrorResponse")
    void shouldReturn404WhenEventDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findByCode("EVT-999"))
                .thenThrow(new ResourceNotFoundException("Event not found: EVT-999"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/{eventCode}", "EVT-999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event not found: EVT-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("EVT-999");
    }

    // ------------------------------------------------------------------
    // GET /api/events/published
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-005: published events returns 200 and a list")
    void shouldReturnPublishedEvents() throws Exception {
        // ARRANGE
        when(service.findPublishedEvents()).thenReturn(List.of(eventSummary(), eventSummary()));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"))
                .andExpect(jsonPath("$[0].venueName").value("Marina Convention Center"));

        verify(service).findPublishedEvents();
        // "published" must not be captured as {eventCode}
        verify(service, never()).findByCode(anyString());
    }

    // ------------------------------------------------------------------
    // PATCH /api/events/{eventCode}/publish
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-006: valid publish returns 200")
    void shouldPublishEvent() throws Exception {
        // ARRANGE
        when(service.publish(EVENT_CODE)).thenReturn(eventResponse(EventStatus.PUBLISHED, List.of()));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/events/{eventCode}/publish", EVENT_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(service).publish(EVENT_CODE);
    }

    @Test
    @DisplayName("TEST-CTRL-EVT-007: invalid publish returns 409 with ErrorResponse")
    void shouldReturn409WhenEventCannotBePublished() throws Exception {
        // ARRANGE
        when(service.publish(EVENT_CODE))
                .thenThrow(new BusinessRuleException("Only DRAFT events can be published: CMF-2026"));

        // ACT + ASSERT
        mockMvc.perform(patch("/api/events/{eventCode}/publish", EVENT_CODE))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Only DRAFT events can be published: CMF-2026"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).publish(EVENT_CODE);
    }

    // ------------------------------------------------------------------
    // POST /api/events/{eventCode}/artists/{artistId}
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-008: add artist returns 200")
    void shouldAddArtistToEvent() throws Exception {
        // ARRANGE
        when(service.addArtist(EVENT_CODE, 1L))
                .thenReturn(eventResponse(EventStatus.DRAFT, List.of("Solar Beat")));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", EVENT_CODE, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.artists[0]").value("Solar Beat"));

        verify(service).addArtist(EVENT_CODE, 1L);
    }

    @Test
    @DisplayName("BR-EVENT-010: artist already associated returns 409")
    void shouldReturn409WhenArtistIsAlreadyAssociated() throws Exception {
        // ARRANGE
        when(service.addArtist(EVENT_CODE, 1L))
                .thenThrow(new BusinessRuleException("Artist already associated with event: CMF-2026"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", EVENT_CODE, 1L))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Artist already associated with event: CMF-2026"));

        verify(service).addArtist(EVENT_CODE, 1L);
    }

    @Test
    void shouldReturn400WhenArtistIdIsNotNumeric() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", EVENT_CODE, "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.details.artistId").value("Invalid value: abc"));

        verify(service, never()).addArtist(anyString(), anyLong());
    }

    // ------------------------------------------------------------------
    // GET /api/events/by-artist?stageName=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-EVT-009: events by artist returns 200 and a list")
    void shouldReturnEventsByArtist() throws Exception {
        // ARRANGE
        when(service.findByArtist("Solar Beat")).thenReturn(List.of(eventSummary()));

        // ACT + ASSERT
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(service).findByArtist("Solar Beat");
        verify(service, never()).findByCode(anyString());
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
