package com.pulse.pass.service;

import com.pulse.pass.domain.*;
import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.mapper.TicketMapper;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final String USER_EMAIL = "andrea@email.com";
    private static final String EVENT_CODE = "CMF-2026";
    private static final String TICKET_CODE = "TCK-CMF-2026-1";
    private static final int CAPACITY = 3;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketMapper mapper;

    @InjectMocks
    private TicketServiceImpl service;

    // ------------------------------------------------------------------
    // purchase - happy paths
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-TICKET-001: valid purchase creates a PAID ticket")
    void purchase_validRequest_createsPaidTicket() {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 25));
        TicketResponse expected = ticketResponse(TicketStatus.PAID);
        givenPurchaseScenario(user, event, 0L, expected);

        // ACT
        TicketResponse result = service.purchase(purchaseRequest(TicketType.VIP));

        // ASSERT
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(saved.getType()).isEqualTo(TicketType.VIP);
        assertThat(saved.getUser()).isSameAs(user);
        assertThat(saved.getEvent()).isSameAs(event);
        assertThat(saved.getTicketCode()).startsWith("TCK-" + EVENT_CODE);
        assertThat(saved.getPurchaseDate()).isNotNull();
        assertThat(result).isEqualTo(expected);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private void givenPurchaseScenario(User user, Event event, long paidTickets, TicketResponse response) {
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus(eq(EVENT_CODE), eq(TicketStatus.PAID)))
                .thenReturn(paidTickets);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(Ticket.class))).thenReturn(response);
    }

    private PurchaseTicketRequest purchaseRequest(TicketType type) {
        return new PurchaseTicketRequest(USER_EMAIL, EVENT_CODE, type);
    }

    private LocalDateTime futureDate() {
        return LocalDateTime.now().plusDays(30);
    }

    private Venue venue() {
        return new Venue("VEN-SMR-01", "Marina Convention Center", "Santa Marta",
                "Calle 1 #1-01", CAPACITY, true);
    }

    private Event event(EventStatus status, LocalDateTime date, int minimumAge) {
        Event event = new Event(EVENT_CODE, "Caribbean Music Fest 2026", "Festival de musica",
                EventCategory.MUSIC, status, date, minimumAge);
        event.setVenue(venue());
        return event;
    }

    private Event publishedEvent(int minimumAge) {
        return event(EventStatus.PUBLISHED, futureDate(), minimumAge);
    }

    private User user(boolean active, LocalDate birthDate) {
        User user = new User("andrea", USER_EMAIL, active);
        user.assignProfile(new UserProfile("Andrea", "Lopez", "3001234567", "Santa Marta", birthDate));
        return user;
    }

    private LocalDate birthDateForAge(Event event, int years) {
        return event.getEventDate().toLocalDate().minusYears(years);
    }

    private TicketResponse ticketResponse(TicketStatus status) {
        return new TicketResponse(1L, TICKET_CODE, TicketType.VIP, new BigDecimal("240000.00"),
                status, LocalDateTime.now(), USER_EMAIL, EVENT_CODE, "Caribbean Music Fest 2026");
    }
}