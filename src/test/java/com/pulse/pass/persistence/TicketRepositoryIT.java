package com.pulse.pass.persistence;

import com.pulse.pass.domain.*;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.repository.VenueRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@Transactional
class TicketRepositoryIT {

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
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    // QT-006 / FR-TKT-001 / AC-002
    @Test
    void shouldPersistTicketWithUserAndEvent() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-01", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "CMF-2026", "Caribbean Music Fest 2026", "Festival de música caribeña",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        User user = userRepository.save(new User("andrea.gomez", "andrea.gomez@pulsepass.com", true));

        Ticket ticket = new Ticket(
                user, event, "TCK-0001", TicketType.VIP,
                new BigDecimal("250000.00"), TicketStatus.PAID, LocalDateTime.now()
        );
        ticketRepository.save(ticket);

        Ticket retrieved = ticketRepository.findByTicketCode("TCK-0001").orElseThrow();

        assertThat(retrieved.getUser().getUsername()).isEqualTo("andrea.gomez");
        assertThat(retrieved.getEvent().getEventCode()).isEqualTo("CMF-2026");
        assertThat(retrieved.getPrice()).isEqualByComparingTo(new BigDecimal("250000.00"));
    }

    // QT-007 / FR-TKT-006
    @Test
    void shouldFindTicketsByUserEmailAndStatus() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-02", "Movistar Arena", "Bogotá",
                "Calle 63 #47-33", 15000, true
        ));

        Event event = new Event(
                "TCK-EVT-01", "Ticket Test Fest", "Evento de prueba",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 11, 1, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        User carlos = userRepository.save(new User("carlos.perez", "carlos.perez@pulsepass.com", true));

        ticketRepository.saveAll(List.of(
                new Ticket(carlos, event, "TCK-1001", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now()),
                new Ticket(carlos, event, "TCK-1002", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.RESERVED, LocalDateTime.now())
        ));

        List<Ticket> allTicketsForCarlos = ticketRepository.findByUser_Email("carlos.perez@pulsepass.com");
        assertThat(allTicketsForCarlos).hasSize(2);

        List<Ticket> paidTicketsForCarlos = ticketRepository
                .findByUser_EmailAndStatus("carlos.perez@pulsepass.com", TicketStatus.PAID);
        assertThat(paidTicketsForCarlos)
                .extracting(Ticket::getTicketCode)
                .containsExactly("TCK-1001");
    }

    // FR-TKT-007 / UC-08
    @Test
    void shouldFindPaidTicketsByEventCode() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-03", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "CMF-2026-SALES", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        User laura = userRepository.save(new User("laura.diaz", "laura.diaz@pulsepass.com", true));
        User miguel = userRepository.save(new User("miguel.torres", "miguel.torres@pulsepass.com", true));

        ticketRepository.saveAll(List.of(
                new Ticket(laura, event, "TCK-2001", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now()),
                new Ticket(miguel, event, "TCK-2002", TicketType.VIP,
                        new BigDecimal("250000.00"), TicketStatus.CANCELLED, LocalDateTime.now())
        ));

        List<Ticket> paidTickets = ticketRepository
                .findByEvent_EventCodeAndStatus("CMF-2026-SALES", TicketStatus.PAID);

        assertThat(paidTickets)
                .extracting(Ticket::getTicketCode)
                .containsExactly("TCK-2001");
    }

    // FR-TKT-008 / AC-008
    @Test
    void shouldCountOnlyPaidTicketsForEvent() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-04", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "CMF-2026-COUNT", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        User andrea = userRepository.save(new User("andrea.count", "andrea.count@pulsepass.com", true));
        User carlos = userRepository.save(new User("carlos.count", "carlos.count@pulsepass.com", true));
        User laura = userRepository.save(new User("laura.count", "laura.count@pulsepass.com", true));

        ticketRepository.saveAll(List.of(
                new Ticket(andrea, event, "TCK-3001", TicketType.VIP,
                        new BigDecimal("250000.00"), TicketStatus.PAID, LocalDateTime.now()),
                new Ticket(carlos, event, "TCK-3002", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now()),
                new Ticket(laura, event, "TCK-3003", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.RESERVED, LocalDateTime.now())
        ));

        long paidCount = ticketRepository.countPaidByEventCode("CMF-2026-COUNT");

        assertThat(paidCount).isEqualTo(2);
    }

    // FR-SRC-004
    @Test
    void shouldFindTicketsForFutureEventsOrderedChronologically() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-05", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event nearEvent = new Event(
                "EVT-NEAR", "Near Event", "Evento cercano",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 12, 1, 20, 0), 0
        );
        Event farEvent = new Event(
                "EVT-FAR", "Far Event", "Evento lejano",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2027, 3, 1, 20, 0), 0
        );
        venue.addEvent(nearEvent);
        venue.addEvent(farEvent);
        eventRepository.saveAll(List.of(nearEvent, farEvent));

        User user = userRepository.save(new User("miguel.future", "miguel.future@pulsepass.com", true));

        ticketRepository.saveAll(List.of(
                new Ticket(user, farEvent, "TCK-4001", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now()),
                new Ticket(user, nearEvent, "TCK-4002", TicketType.GENERAL,
                        new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now())
        ));

        List<Ticket> futureTickets = ticketRepository
                .findByEvent_EventDateAfterOrderByEvent_EventDateAsc(LocalDateTime.of(2026, 1, 1, 0, 0));

        assertThat(futureTickets)
                .extracting(Ticket::getTicketCode)
                .containsExactly("TCK-4002", "TCK-4001");
    }

    // QT-009 / AC-005
    @Test
    void shouldViolateUniqueConstraintOnDuplicateTicketCode() {

        Venue venue = venueRepository.save(new Venue(
                "VEN-TKT-06", "Marina Convention Center", "Santa Marta",
                "Carrera 1 #5-30", 5000, true
        ));

        Event event = new Event(
                "EVT-DUP", "Duplicate Test Event", "Evento de prueba",
                EventCategory.MUSIC, EventStatus.PUBLISHED,
                LocalDateTime.of(2026, 10, 15, 20, 0), 0
        );
        venue.addEvent(event);
        eventRepository.save(event);

        User user1 = userRepository.save(new User("user.one", "user.one@pulsepass.com", true));
        User user2 = userRepository.save(new User("user.two", "user.two@pulsepass.com", true));

        Ticket firstTicket = new Ticket(
                user1, event, "TCK-0001", TicketType.GENERAL,
                new BigDecimal("120000.00"), TicketStatus.PAID, LocalDateTime.now()
        );
        ticketRepository.saveAndFlush(firstTicket);

        Ticket duplicateTicket = new Ticket(
                user2, event, "TCK-0001", TicketType.VIP,
                new BigDecimal("250000.00"), TicketStatus.PAID, LocalDateTime.now()
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> ticketRepository.saveAndFlush(duplicateTicket)
        );
    }
}