package com.pulse.pass.service;

import com.pulse.pass.domain.Venue;
import com.pulse.pass.dto.response.VenueResponse;
import com.pulse.pass.mapper.VenueMapper;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.impl.VenueServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VenueServiceImplTest {

    private static final String VENUE_CODE = "VEN-SMR-01";

    @Mock
    private VenueRepository repository;

    @Mock
    private VenueMapper mapper;

    @InjectMocks
    private VenueServiceImpl service;

    // ------------------------------------------------------------------
    // findByCode
    // ------------------------------------------------------------------

    @Test
    @DisplayName("FR-SVC-001: existing venue returns DTO")
    void findByCode_existingVenue_returnsDto() {
        // ARRANGE
        Venue venue = venue(VENUE_CODE, "Marina Convention Center", true);
        VenueResponse expected = venueResponse(VENUE_CODE, "Marina Convention Center", true);
        when(repository.findByCode(eq(VENUE_CODE))).thenReturn(Optional.of(venue));
        when(mapper.toResponse(venue)).thenReturn(expected);

        // ACT
        VenueResponse result = service.findByCode(VENUE_CODE);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(repository).findByCode(VENUE_CODE);
        verify(mapper).toResponse(venue);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Venue venue(String code, String name, boolean active) {
        return new Venue(code, name, "Santa Marta", "Calle 1 #1-01", 3, active);
    }

    private VenueResponse venueResponse(String code, String name, boolean active) {
        return new VenueResponse(1L, code, name, "Santa Marta", "Calle 1 #1-01", 3, active);
    }
}