package ServiceImplTest;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.Venue;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.dto.response.EventSummaryResponse;
import com.pulse.pass.exception.ResourceNotFoundException;
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
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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

    @Test
    @DisplayName("TEST-EVENT-002: missing event throws ResourceNotFoundException")
    void findByCode_missingEvent_throwsResourceNotFound() {
        // ARRANGE
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.findByCode(EVENT_CODE));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_CODE);
        verify(mapper, never()).toResponse(any(Event.class));
    }

    @Test
    void findPublishedEvents_returnsMappedSummaries() {
        // ARRANGE
        Event event = event(EventStatus.PUBLISHED, futureDate(), venue(true));
        EventSummaryResponse summary = eventSummary();
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED))
                .thenReturn(List.of(event));
        when(mapper.toSummary(event)).thenReturn(summary);

        // ACT
        List<EventSummaryResponse> result = service.findPublishedEvents();

        // ASSERT
        assertThat(result).containsExactly(summary);
        verify(eventRepository).findByStatusOrderByEventDateAsc(eq(EventStatus.PUBLISHED));
    }

    @Test
    void findPublishedEvents_noEvents_returnsEmptyList() {
        // ARRANGE
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED))
                .thenReturn(List.of());

        // ACT
        List<EventSummaryResponse> result = service.findPublishedEvents();

        // ASSERT
        assertThat(result).isEmpty();
        verify(mapper, never()).toSummary(any(Event.class));
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

    private EventSummaryResponse eventSummary() {
        return new EventSummaryResponse(1L, EVENT_CODE, "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, futureDate(), "Marina Convention Center");
    }


}