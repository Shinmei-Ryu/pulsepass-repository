package com.pulse.pass.service;

import com.pulse.pass.domain.Artist;
import com.pulse.pass.dto.response.ArtistResponse;
import com.pulse.pass.mapper.ArtistMapper;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.service.impl.ArtistServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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