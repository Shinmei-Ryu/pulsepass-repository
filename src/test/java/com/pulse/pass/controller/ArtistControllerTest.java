package com.pulse.pass.controller;

import com.pulse.pass.dto.response.ArtistResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.service.ArtistService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ArtistController.class)
@Import(GlobalExceptionHandler.class)
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService service;

    @Test
    @DisplayName("TEST-CTRL-ART-001: find by id returns 200")
    void shouldReturnArtistById() throws Exception {
        // ARRANGE
        when(service.findById(1L)).thenReturn(artist(1L, "Solar Beat"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"))
                .andExpect(jsonPath("$.country").value("Colombia"))
                .andExpect(jsonPath("$.genre").value("Pop"))
                .andExpect(jsonPath("$.active").value(true));

        verify(service).findById(1L);
    }

    @Test
    @DisplayName("TEST-CTRL-ART-002: missing id returns 404 with ErrorResponse")
    void shouldReturn404WhenArtistIdDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findById(99L))
                .thenThrow(new ResourceNotFoundException("Artist not found: 99"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Artist not found: 99"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findById(99L);
    }


    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ArtistResponse artist(Long id, String stageName) {
        return new ArtistResponse(id, stageName, "Colombia", "Pop", true);
    }
}
