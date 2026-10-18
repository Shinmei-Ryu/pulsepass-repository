package com.pulse.pass.controller;

import com.pulse.pass.dto.response.ArtistResponse;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.security.CustomUserDetailsService;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.security.SecurityConfig;
import com.pulse.pass.service.ArtistService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ArtistController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class ArtistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArtistService service;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ------------------------------------------------------------------
    // GET /api/artists/{id}
    // ------------------------------------------------------------------


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

    @Test
    void shouldReturn400WhenArtistIdIsNotNumeric() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request parameter"))
                .andExpect(jsonPath("$.details.id").value("Invalid value: abc"));

        verify(service, never()).findById(anyLong());
    }

    // ------------------------------------------------------------------
    // GET /api/artists/by-stage-name?stageName=...
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-ART-003: find by stage name returns 200")
    void shouldReturnArtistByStageName() throws Exception {
        // ARRANGE
        when(service.findByStageName("Solar Beat")).thenReturn(artist(1L, "Solar Beat"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.stageName").value("Solar Beat"));

        verify(service).findByStageName("Solar Beat");
        verify(service, never()).findById(anyLong());
    }

    @Test
    void shouldReturn404WhenStageNameDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findByStageName("Nobody"))
                .thenThrow(new ResourceNotFoundException("Artist not found: Nobody"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/by-stage-name").param("stageName", "Nobody"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Artist not found: Nobody"));

        verify(service).findByStageName("Nobody");
    }

    @Test
    void shouldReturn400WhenStageNameParamIsMissing() throws Exception {
        mockMvc.perform(get("/api/artists/by-stage-name"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Missing request parameter"))
                .andExpect(jsonPath("$.details.stageName").value("Parameter is required"));

        verify(service, never()).findByStageName(anyString());
    }

    @Test
    void shouldReturn500WhenUnexpectedErrorOccurs() throws Exception {
        when(service.findById(1L)).thenThrow(new RuntimeException("internal failure details"));

        mockMvc.perform(get("/api/artists/{id}", 1L))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").isMap());
    }

    // ------------------------------------------------------------------
    // GET /api/artists/active
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-ART-004: active artists returns 200 and a list")
    void shouldReturnActiveArtists() throws Exception {
        // ARRANGE
        when(service.findActiveArtists()).thenReturn(List.of(
                artist(1L, "Neon Waves"),
                artist(2L, "Solar Beat")));

        // ACT + ASSERT
        mockMvc.perform(get("/api/artists/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stageName").value("Neon Waves"))
                .andExpect(jsonPath("$[1].stageName").value("Solar Beat"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(service).findActiveArtists();
        // "active" must not be captured as {id}
        verify(service, never()).findById(anyLong());
        verify(service, never()).findByStageName(anyString());
    }


    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private ArtistResponse artist(Long id, String stageName) {
        return new ArtistResponse(id, stageName, "Colombia", "Pop", true);
    }
}
