package com.pulse.pass.persistence;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventCategory;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.Venue;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.VenueRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class EventRepositoryIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    // FR-EVT-001 / FR-EVT-003 / FR-EVT-004 / AC-002
    @Test
    void shouldRegisterEventAssociatedToVenueWithValidEnums() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-EVT-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival de música caribeña",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        Event retrieved = eventRepository.findByEventCode("CMF-2026").orElseThrow();

        assertThat(retrieved.getVenue().getCode()).isEqualTo("VEN-EVT-01");
        assertThat(retrieved.getCategory()).isEqualTo(EventCategory.MUSIC);
        assertThat(retrieved.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    // FR-EVT-005 / UC-06 / AC-006
    @Test
    void shouldFindPublishedEventsOrderedByDateAscendingExcludingOtherStatuses() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-EVT-02", "Movistar Arena", "Bogotá",
                "Calle 63 #47-33", 15000, true
        ));

        Event draft = new Event(
                "EVT-DRAFT", "Draft Event", "Aún no publicado",
                EventCategory.TECHNOLOGY, EventStatus.DRAFT,
                LocalDateTime.of(2026, 9, 1, 9, 0), 0
        );
        Event publishedLater = new Event(
                "EVT-PUB-LATE", "Published Later", "Publicado, fecha tardía",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 1, 20, 0), 0
        );
        Event publishedEarlier = new Event(
                "EVT-PUB-EARLY", "Published Earlier", "Publicado, fecha temprana",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 1, 20, 0), 0
        );
        Event cancelled = new Event(
                "EVT-CANCELLED", "Cancelled Event", "Cancelado",
                EventCategory.SPORTS, EventStatus.CANCELLED,
                LocalDateTime.of(2026, 11, 1, 20, 0), 0
        );

        venue.addEvent(draft);
        venue.addEvent(publishedLater);
        venue.addEvent(publishedEarlier);
        venue.addEvent(cancelled);
        eventRepository.saveAll(List.of(draft, publishedLater, publishedEarlier, cancelled));

        List<Event> published = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(published)
                .extracting(Event::getEventCode)
                .containsExactly("EVT-PUB-EARLY", "EVT-PUB-LATE");
    }

    // FR-VEN-004
    @Test
    void shouldFindEventsOnlyFromRequestedVenue() {

        Venue caribbeanVenue = venueRepository.save(new Venue(
                "VEN-CAR-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));
        Venue bogotaVenue = venueRepository.save(new Venue(
                "VEN-BOG-01", "Movistar Arena", "Bogotá",
                "Calle 63 #47-33", 15000, true
        ));

        Event caribbeanEvent = new Event(
                "EVT-CAR-01", "Caribbean Event", "Evento en Santa Marta",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        Event bogotaEvent = new Event(
                "EVT-BOG-01", "Bogotá Event", "Evento en Bogotá",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 1, 20, 0), 0
        );

        caribbeanVenue.addEvent(caribbeanEvent);
        bogotaVenue.addEvent(bogotaEvent);
        eventRepository.saveAll(List.of(caribbeanEvent, bogotaEvent));

        List<Event> eventsFromCaribbean = eventRepository.findByVenue_Code("VEN-CAR-01");

        assertThat(eventsFromCaribbean)
                .extracting(Event::getEventCode)
                .containsExactly("EVT-CAR-01")
                .doesNotContain("EVT-BOG-01");
    }
}
