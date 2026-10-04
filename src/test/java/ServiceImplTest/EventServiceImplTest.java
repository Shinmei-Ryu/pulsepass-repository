package ServiceImplTest;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.Venue;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.mapper.EventMapper;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.impl.EventServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final String EVENT_CODE = "CMF-2026";
    private static final String VENUE_CODE = "VEN-SMR-01";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private EventMapper mapper;

    @InjectMocks
    private EventServiceImpl service;

    // ------------------------------------------------------------------
    // findByCode
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-EVENT-001: existing event returns DTO")
    void findByCode_existingEvent_returnsDto() {
        // ARRANGE
        Event event = event(EventStatus.PUBLISHED, futureDate(), venue(true));
        EventResponse expected = eventResponse(EventStatus.PUBLISHED);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(mapper.toResponse(event)).thenReturn(expected);

        // ACT
        EventResponse result = service.findByCode(EVENT_CODE);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(eventRepository).findByEventCode(EVENT_CODE);
        verify(mapper).toResponse(event);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private LocalDateTime futureDate() {
        return LocalDateTime.now().plusDays(30);
    }

    private Venue venue(boolean active) {
        return new Venue(VENUE_CODE, "Marina Convention Center", "Santa Marta",
                "Calle 1 #1-01", 3, active);
    }

    private Event event(EventStatus status, LocalDateTime date, Venue venue) {
        Event event = new Event(EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, status, date, 18);
        event.setVenue(venue);
        return event;
    }

    private EventResponse eventResponse(EventStatus status) {
        return new EventResponse(1L, EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, status, futureDate(), 18, VENUE_CODE,
                "Marina Convention Center", List.of());
    }


}