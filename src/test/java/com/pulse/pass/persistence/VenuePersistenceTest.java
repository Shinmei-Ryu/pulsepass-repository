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
class VenuePersistenceTest {

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

    // AC-001
    @Test
    void shouldPersistAndRetrieveVenueByCode() {

        Venue venue = new Venue(
                "VEN-SMR-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        );

        venueRepository.save(venue);

        Venue retrieved = venueRepository.findByCode("VEN-SMR-01").orElseThrow();

        assertThat(retrieved.getId()).isNotNull();
        assertThat(retrieved.getCapacity()).isGreaterThan(0);
    }

    // QT-003
    @Test
    void shouldPersistVenueWithMultipleEvents() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-BOG-01", "Movistar Arena", "Bogotá",
                "Calle 63 #47-33", 15000, true
        ));

        Event event1 = new Event(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival de música caribeña",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        Event event2 = new Event(
                "TECH-SUMMIT-2026", "Tech Summit 2026", "Conferencia de tecnología",
                EventCategory.TECHNOLOGY, EventStatus.DRAFT,
                LocalDateTime.of(2026, 11, 5, 9, 0), 0
        );

        venue.addEvent(event1);
        venue.addEvent(event2);
        eventRepository.saveAll(List.of(event1, event2));   // persistencia real, explícita

        Venue retrieved = venueRepository.findByCode("VEN-BOG-01").orElseThrow();

        assertThat(retrieved.getEvents()).hasSize(2);
        assertThat(retrieved.getEvents())
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("CMF-2026", "TECH-SUMMIT-2026");
    }
}