package com.pulse.pass.persistence;

import com.pulse.pass.domain.*;
import com.pulse.pass.repository.ArtistRepository;
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
class EventSearchIT {

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

    @Autowired
    private ArtistRepository artistRepository;

    // FR-SRC-001 / UC-07 / AC-007
    @Test
    void shouldFindAllEventsForArtistWithoutDuplicates() {

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();

        Venue venue = venueRepository.save(new Venue(
                "VEN-SRC-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event1 = new Event(
                "EVT-SRC-01", "Solar Beat Night 1", "Concierto 1",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        Event event2 = new Event(
                "EVT-SRC-02", "Solar Beat Night 2", "Concierto 2",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 15, 20, 0), 0
        );

        event1.addArtist(solarBeat);
        event2.addArtist(solarBeat);
        venue.addEvent(event1);
        venue.addEvent(event2);
        eventRepository.saveAll(List.of(event1, event2));

        List<Event> events = eventRepository.findByArtistStageName("Solar Beat");

        assertThat(events)
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder("EVT-SRC-01", "EVT-SRC-02");
    }

    // FR-SRC-002
    @Test
    void shouldFindEventsByCityAndArtistStageName() {

        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();

        Venue santaMartaVenue = venueRepository.save(new Venue(
                "VEN-SRC-02", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));
        Venue bogotaVenue = venueRepository.save(new Venue(
                "VEN-SRC-03", "Movistar Arena", "Bogotá",
                "Calle 63 #47-33", 15000, true
        ));

        Event eventInSantaMarta = new Event(
                "EVT-SRC-03", "Neon Waves Santa Marta", "Concierto en Santa Marta",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 20, 20, 0), 0
        );
        Event eventInBogota = new Event(
                "EVT-SRC-04", "Neon Waves Bogotá", "Concierto en Bogotá",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 20, 20, 0), 0
        );

        eventInSantaMarta.addArtist(neonWaves);
        eventInBogota.addArtist(neonWaves);
        santaMartaVenue.addEvent(eventInSantaMarta);
        bogotaVenue.addEvent(eventInBogota);
        eventRepository.saveAll(List.of(eventInSantaMarta, eventInBogota));

        List<Event> events = eventRepository.findByCityAndArtistStageName("Santa Marta", "Neon Waves");

        assertThat(events)
                .extracting(Event::getEventCode)
                .containsExactly("EVT-SRC-03");
    }

    // FR-SRC-003 / UC-09
    @Test
    void shouldFindRecommendedEventsPublishedAfterDateInCityWithMatchingArtistText() {

        Artist caribbeanSound = artistRepository.findByStageName("Caribbean Sound").orElseThrow();

        Venue venue = venueRepository.save(new Venue(
                "VEN-SRC-04", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event matching = new Event(
                "EVT-SRC-05", "Caribbean Sound Live", "Concierto que cumple todos los filtros",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 1, 20, 0), 0
        );
        Event wrongStatus = new Event(
                "EVT-SRC-06", "Caribbean Sound Draft", "Mismo artista, no publicado",
                EventCategory.MUSIC, EventStatus.DRAFT,
                LocalDateTime.of(2026, 12, 5, 20, 0), 0
        );
        Event tooEarly = new Event(
                "EVT-SRC-07", "Caribbean Sound Early", "Mismo artista, fecha anterior",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 1, 1, 20, 0), 0
        );

        matching.addArtist(caribbeanSound);
        wrongStatus.addArtist(caribbeanSound);
        tooEarly.addArtist(caribbeanSound);
        venue.addEvent(matching);
        venue.addEvent(wrongStatus);
        venue.addEvent(tooEarly);
        eventRepository.saveAll(List.of(matching, wrongStatus, tooEarly));

        List<Event> recommended = eventRepository.findRecommendedEvents(
                LocalDateTime.of(2026, 6, 1, 0, 0),
                "Santa Marta",
                "caribbean"
        );

        assertThat(recommended)
                .extracting(Event::getEventCode)
                .containsExactly("EVT-SRC-05");
    }
}
