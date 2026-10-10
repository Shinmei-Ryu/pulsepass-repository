package com.pulse.pass.controller;

import com.pulse.pass.dto.response.VenueResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.service.VenueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VenueController.class)
@Import(GlobalExceptionHandler.class)
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

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private VenueResponse venue(String code, String name) {
        return new VenueResponse(1L, code, name, "Santa Marta", "Calle 1 #1-01", 3, true);
    }
}
