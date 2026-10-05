package com.pulse.pass.service;

import com.pulse.pass.domain.Artist;
import com.pulse.pass.dto.response.ArtistResponse;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.ArtistMapper;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.service.impl.ArtistServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceImplTest {

    private static final Long ARTIST_ID = 1L;
    private static final String STAGE_NAME = "Solar Beat";

    @Mock
    private ArtistRepository repository;

    @Mock
    private ArtistMapper mapper;

    @InjectMocks
    private ArtistServiceImpl service;

    // ------------------------------------------------------------------
    // findById
    // ------------------------------------------------------------------

    @Test
    @DisplayName("FR-SVC-009: existing artist by id returns DTO")
    void findById_existingArtist_returnsDto() {
        // ARRANGE
        Artist artist = artist(STAGE_NAME, true);
        ArtistResponse expected = artistResponse(STAGE_NAME, true);
        when(repository.findById(ARTIST_ID)).thenReturn(Optional.of(artist));
        when(mapper.toResponse(artist)).thenReturn(expected);

        // ACT
        ArtistResponse result = service.findById(ARTIST_ID);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(repository).findById(ARTIST_ID);
        verify(mapper).toResponse(artist);
    }

    @Test
    @DisplayName("BR-ARTIST-001: missing artist by id throws ResourceNotFoundException")
    void findById_missingArtist_throwsResourceNotFound() {
        // ARRANGE
        when(repository.findById(ARTIST_ID)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.findById(ARTIST_ID));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(String.valueOf(ARTIST_ID));
        verify(mapper, never()).toResponse(any(Artist.class));
    }

    // ------------------------------------------------------------------
    // findByStageName
    // ------------------------------------------------------------------

    @Test
    @DisplayName("FR-SVC-009: existing artist by stage name returns DTO")
    void findByStageName_existingArtist_returnsDto() {
        // ARRANGE
        Artist artist = artist(STAGE_NAME, true);
        ArtistResponse expected = artistResponse(STAGE_NAME, true);
        when(repository.findByStageNameIgnoreCase(eq(STAGE_NAME))).thenReturn(Optional.of(artist));
        when(mapper.toResponse(artist)).thenReturn(expected);

        // ACT
        ArtistResponse result = service.findByStageName(STAGE_NAME);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(repository).findByStageNameIgnoreCase(STAGE_NAME);
        verify(mapper).toResponse(artist);
    }

    @Test
    @DisplayName("BR-ARTIST-001: missing artist by stage name throws ResourceNotFoundException")
    void findByStageName_missingArtist_throwsResourceNotFound() {
        // ARRANGE
        when(repository.findByStageNameIgnoreCase(STAGE_NAME)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.findByStageName(STAGE_NAME));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(STAGE_NAME);
        verify(mapper, never()).toResponse(any(Artist.class));
    }

    // ------------------------------------------------------------------
    // findActiveArtists
    // ------------------------------------------------------------------

    @Test
    @DisplayName("BR-ARTIST-002: findActiveArtists returns mapped active artists")
    void findActiveArtists_returnsMappedActiveArtists() {
        // ARRANGE
        Artist solarBeat = artist("Solar Beat", true);
        Artist neonWaves = artist("Neon Waves", true);
        ArtistResponse solarBeatResponse = artistResponse("Solar Beat", true);
        ArtistResponse neonWavesResponse = artistResponse("Neon Waves", true);
        when(repository.findByActiveTrueOrderByStageNameAsc()).thenReturn(List.of(neonWaves, solarBeat));
        when(mapper.toResponse(solarBeat)).thenReturn(solarBeatResponse);
        when(mapper.toResponse(neonWaves)).thenReturn(neonWavesResponse);

        // ACT
        List<ArtistResponse> result = service.findActiveArtists();

        // ASSERT
        assertThat(result).containsExactly(neonWavesResponse, solarBeatResponse);
        verify(repository).findByActiveTrueOrderByStageNameAsc();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Artist artist(String stageName, boolean active) {
        return new Artist(stageName, "Colombia", "Pop", active);
    }

    private ArtistResponse artistResponse(String stageName, boolean active) {
        return new ArtistResponse(ARTIST_ID, stageName, "Colombia", "Pop", active);
    }
}