package ServiceImplTest;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.Venue;
import com.pulse.pass.dto.request.CreateEventRequest;
import com.pulse.pass.dto.response.EventResponse;
import com.pulse.pass.dto.response.EventSummaryResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.DuplicateResourceException;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.EventMapper;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.impl.EventServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.*;
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
    // create
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-EVENT-003 / BR-EVENT-005: valid request saves event as DRAFT")
    void create_validRequest_savesDraftEvent() {
        // ARRANGE
        Venue venue = venue(true);
        CreateEventRequest request = createRequest(futureDate(), 18);
        EventResponse expected = eventResponse(EventStatus.DRAFT);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(eq(VENUE_CODE))).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenReturn(expected);

        // ACT
        EventResponse result = service.create(request);

        // ASSERT
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getEventCode()).isEqualTo(EVENT_CODE);
        assertThat(saved.getName()).isEqualTo(request.name());
        assertThat(saved.getCategory()).isEqualTo(EventCategory.MUSIC);
        assertThat(saved.getMinimumAge()).isEqualTo(18);
        assertThat(saved.getVenue()).isSameAs(venue);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("BR-EVENT-006: minimumAge 0 means no restriction and is accepted")
    void create_minimumAgeZero_isAccepted() {
        // ARRANGE
        CreateEventRequest request = createRequest(futureDate(), 0);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue(true)));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Event.class))).thenReturn(eventResponse(EventStatus.DRAFT));

        // ACT
        service.create(request);

        // ASSERT
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getMinimumAge()).isZero();
    }

    @Test
    @DisplayName("BR-EVENT-001: duplicated eventCode throws DuplicateResourceException")
    void create_duplicatedEventCode_throwsDuplicateResource() {
        // ARRANGE
        CreateEventRequest request = createRequest(futureDate(), 18);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(true);

        // ACT
        Throwable thrown = catchThrowable(() -> service.create(request));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(EVENT_CODE);
        verify(venueRepository, never()).findByCode(anyString());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("TEST-EVENT-004: missing venue throws ResourceNotFoundException and never saves")
    void create_missingVenue_throwsResourceNotFoundAndNeverSaves() {
        // ARRANGE
        CreateEventRequest request = createRequest(futureDate(), 18);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.create(request));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(VENUE_CODE);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("TEST-EVENT-005: inactive venue throws BusinessRuleException")
    void create_inactiveVenue_throwsBusinessRule() {
        // ARRANGE
        CreateEventRequest request = createRequest(futureDate(), 18);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue(false)));

        // ACT
        Throwable thrown = catchThrowable(() -> service.create(request));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("TEST-EVENT-006: past date throws BusinessRuleException")
    void create_pastDate_throwsBusinessRule() {
        // ARRANGE
        CreateEventRequest request = createRequest(LocalDateTime.now().minusDays(1), 18);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue(true)));

        // ACT
        Throwable thrown = catchThrowable(() -> service.create(request));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("BR-EVENT-006: negative minimumAge throws BusinessRuleException")
    void create_negativeMinimumAge_throwsBusinessRule() {
        // ARRANGE
        CreateEventRequest request = createRequest(futureDate(), -1);
        when(eventRepository.existsByEventCode(EVENT_CODE)).thenReturn(false);
        when(venueRepository.findByCode(VENUE_CODE)).thenReturn(Optional.of(venue(true)));

        // ACT
        Throwable thrown = catchThrowable(() -> service.create(request));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ------------------------------------------------------------------
    // publish
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-EVENT-007: valid DRAFT event becomes PUBLISHED")
    void publish_validDraftEvent_becomesPublished() {
        // ARRANGE
        Event event = event(EventStatus.DRAFT, futureDate(), venue(true));
        EventResponse expected = eventResponse(EventStatus.PUBLISHED);
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(mapper.toResponse(event)).thenReturn(expected);

        // ACT
        EventResponse result = service.publish(EVENT_CODE);

        // ASSERT
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(result).isEqualTo(expected);
        verify(eventRepository).save(event);
    }

    @Test
    void publish_missingEvent_throwsResourceNotFound() {
        // ARRANGE
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.publish(EVENT_CODE));

        // ASSERT
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"PUBLISHED", "SOLD_OUT", "CANCELLED", "FINISHED"})
    @DisplayName("TEST-EVENT-008 / BR-EVENT-007: non-DRAFT event throws BusinessRuleException and is not persisted")
    void publish_nonDraftEvent_throwsBusinessRuleAndDoesNotPersist(EventStatus status) {
        // ARRANGE
        Event event = event(status, futureDate(), venue(true));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT
        Throwable thrown = catchThrowable(() -> service.publish(EVENT_CODE));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(event.getStatus()).isEqualTo(status);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("BR-EVENT-008: DRAFT event with past date cannot be published")
    void publish_pastDate_throwsBusinessRule() {
        // ARRANGE
        Event event = event(EventStatus.DRAFT, LocalDateTime.now().minusDays(1), venue(true));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT
        Throwable thrown = catchThrowable(() -> service.publish(EVENT_CODE));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(event.getStatus()).isEqualTo(EventStatus.DRAFT);
        verify(eventRepository, never()).save(any(Event.class));
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

    private CreateEventRequest createRequest(LocalDateTime date, Integer minimumAge) {
        return new CreateEventRequest(EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, date, minimumAge, VENUE_CODE);
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