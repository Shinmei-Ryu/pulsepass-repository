package com.pulse.pass.controller;

import com.pulse.pass.dto.response.VenueResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.security.SecurityConfig;
import com.pulse.pass.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class VenueControllerTest {

    private static final String VENUE_CODE = "VEN-SMR-01";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService service;

    @Test
    @DisplayName("TEST-CTRL-VEN-001: existing venue returns 200 and VenueResponse")
    void shouldReturnVenueByCode() throws Exception {
        // ARRANGE
        when(service.findByCode(VENUE_CODE)).thenReturn(venue(VENUE_CODE, "Marina Convention Center"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/{code}", VENUE_CODE))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$.name").value("Marina Convention Center"))
                .andExpect(jsonPath("$.city").value("Santa Marta"))
                .andExpect(jsonPath("$.capacity").value(3))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).findByCode(VENUE_CODE);
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-002: missing venue returns 404 with ErrorResponse")
    void shouldReturn404WhenVenueDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findByCode("VEN-999"))
                .thenThrow(new ResourceNotFoundException("Venue not found: VEN-999"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/{code}", "VEN-999"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Venue not found: VEN-999"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByCode("VEN-999");
    }

    @Test
    @DisplayName("TEST-CTRL-VEN-003: active venues returns 200 and a list")
    void shouldReturnActiveVenues() throws Exception {
        // ARRANGE
        when(service.findActiveVenues()).thenReturn(List.of(
                venue("VEN-SMR-01", "Marina Convention Center"),
                venue("VEN-BOG-01", "Plaza Mayor")));

        // ACT + ASSERT
        mockMvc.perform(get("/api/venues/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("VEN-SMR-01"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[1].code").value("VEN-BOG-01"));

        verify(service).findActiveVenues();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private VenueResponse venue(String code, String name) {
        return new VenueResponse(1L, code, name, "Santa Marta", "Calle 1 #1-01", 3, true);
    }
}
