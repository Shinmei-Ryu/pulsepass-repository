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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@Transactional
class EventArtistIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    // QT-005 / AC-003
    @Test
    void shouldAssociateMultipleArtistsToAnEventWithoutDuplicating() {

        Artist solarBeat = artistRepository.findByStageName("Solar Beat").orElseThrow();
        Artist neonWaves = artistRepository.findByStageName("Neon Waves").orElseThrow();
        Artist caribbeanSound = artistRepository.findByStageName("Caribbean Sound").orElseThrow();

        Venue venue = venueRepository.save(new Venue(
                "VEN-EA-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "EVT-TEST-001", "Test Fest", "Evento de prueba",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 1, 20, 0), 0
        );
        event.setVenue(venue);
        event.addArtist(solarBeat);
        event.addArtist(neonWaves);
        event.addArtist(caribbeanSound);

        eventRepository.save(event);

        Event retrieved = eventRepository.findByEventCode("EVT-TEST-001").orElseThrow();

        assertThat(retrieved.getArtists()).hasSize(3);
        assertThat(retrieved.getArtists())
                .extracting(Artist::getStageName)
                .containsExactlyInAnyOrder("Solar Beat", "Neon Waves", "Caribbean Sound");
    }

    // FR-ART-004 / AC-003 (verificado desde el otro lado de la relación)
    @Test
    void artistShouldNotBeDuplicatedWhenAssociatedToSameEventTwice() {

        Artist oceanDrive = artistRepository.findByStageName("Ocean Drive").orElseThrow();

        Venue venue = venueRepository.save(new Venue(
                "VEN-EA-02", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "EVT-TEST-002", "No Duplicate Fest", "Evento de prueba de duplicados",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 2, 20, 0), 0
        );
        event.setVenue(venue);
        event.addArtist(oceanDrive);
        event.addArtist(oceanDrive);

        eventRepository.save(event);

        Event retrieved = eventRepository.findByEventCode("EVT-TEST-002").orElseThrow();

        assertThat(retrieved.getArtists()).hasSize(1);
    }
}