package com.pulse.pass.service;

import com.pulse.pass.domain.*;
import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.TicketMapper;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.service.impl.TicketServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
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

    @ParameterizedTest
    @CsvSource({
            "GENERAL,120000",
            "STUDENT,60000",
            "VIP,240000",
            "BACKSTAGE,360000"
    })
    @DisplayName("BR-TICKET-009: price strategy per ticket type is never negative")
    void purchase_priceStrategy_matchesTicketType(TicketType type, String expectedPrice) {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 25));
        givenPurchaseScenario(user, event, 0L, ticketResponse(TicketStatus.PAID));

        // ACT
        service.purchase(purchaseRequest(type));

        // ASSERT
        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        assertThat(captor.getValue().getPrice())
                .isEqualByComparingTo(new BigDecimal(expectedPrice))
                .isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("BR-TICKET-006: user turning exactly the minimum age on event date can buy")
    void purchase_userExactlyMinimumAgeAtEventDate_canBuy() {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 18));
        givenPurchaseScenario(user, event, 0L, ticketResponse(TicketStatus.PAID));

        // ACT
        TicketResponse result = service.purchase(purchaseRequest(TicketType.GENERAL));

        // ASSERT
        assertThat(result).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("BR-TICKET-006: minimumAge 0 skips the age validation")
    void purchase_eventWithoutAgeRestriction_skipsAgeCheck() {
        // ARRANGE
        Event event = publishedEvent(0);
        User userWithoutProfile = new User("andrea", USER_EMAIL, true);
        givenPurchaseScenario(userWithoutProfile, event, 0L, ticketResponse(TicketStatus.PAID));

        // ACT
        TicketResponse result = service.purchase(purchaseRequest(TicketType.GENERAL));

        // ASSERT
        assertThat(result).isNotNull();
        verify(ticketRepository).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TEST-TICKET-008 / BR-TICKET-008: last available ticket is saved and event becomes SOLD_OUT")
    void purchase_lastAvailableTicket_marksEventSoldOut() {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 30));
        givenPurchaseScenario(user, event, CAPACITY - 1L, ticketResponse(TicketStatus.PAID));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        // ACT
        service.purchase(purchaseRequest(TicketType.GENERAL));

        // ASSERT
        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
        InOrder inOrder = inOrder(ticketRepository, eventRepository);
        inOrder.verify(ticketRepository).save(any(Ticket.class));
        inOrder.verify(eventRepository).save(event);
    }

    @Test
    @DisplayName("BR-TICKET-008: purchase that does not fill capacity keeps event PUBLISHED")
    void purchase_notLastTicket_keepsEventPublished() {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 30));
        givenPurchaseScenario(user, event, CAPACITY - 2L, ticketResponse(TicketStatus.PAID));

        // ACT
        service.purchase(purchaseRequest(TicketType.GENERAL));

        // ASSERT
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ------------------------------------------------------------------
    // purchase - invalid paths
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-TICKET-002 / BR-TICKET-001: missing user throws ResourceNotFoundException")
    void purchase_missingUser_throwsResourceNotFound() {
        // ARRANGE
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(USER_EMAIL);
        verifyNoInteractions(eventRepository, ticketRepository);
    }

    @Test
    @DisplayName("TEST-TICKET-003 / BR-TICKET-002: inactive user throws BusinessRuleException")
    void purchase_inactiveUser_throwsBusinessRule() {
        // ARRANGE
        User inactiveUser = user(false, LocalDate.now().minusYears(30));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(inactiveUser));

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(eventRepository);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("BR-TICKET-003: missing event throws ResourceNotFoundException")
    void purchase_missingEvent_throwsResourceNotFound() {
        // ARRANGE
        User user = user(true, LocalDate.now().minusYears(30));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EVENT_CODE);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @ParameterizedTest
    @EnumSource(value = EventStatus.class, names = {"DRAFT", "SOLD_OUT", "CANCELLED", "FINISHED"})
    @DisplayName("TEST-TICKET-004 / TEST-TICKET-005 / BR-TICKET-004: non-PUBLISHED event throws BusinessRuleException")
    void purchase_nonPublishedEvent_throwsBusinessRule(EventStatus status) {
        // ARRANGE
        Event event = event(status, futureDate(), 18);
        User user = user(true, LocalDate.now().minusYears(30));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("BR-TICKET-005: past event throws BusinessRuleException")
    void purchase_pastEvent_throwsBusinessRule() {
        // ARRANGE
        Event event = event(EventStatus.PUBLISHED, LocalDateTime.now().minusDays(1), 18);
        User user = user(true, LocalDate.now().minusYears(30));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TEST-TICKET-006 / BR-TICKET-006: underage user throws BusinessRuleException")
    void purchase_underageUser_throwsBusinessRule() {
        // ARRANGE
        Event event = publishedEvent(18);
        User underageUser = user(true, birthDateForAge(event, 17));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(underageUser));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).countByEventEventCodeAndStatus(anyString(), any(TicketStatus.class));
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("TEST-TICKET-007 / BR-TICKET-007 / AC-009: event without capacity throws BusinessRuleException")
    void purchase_eventWithoutCapacity_throwsBusinessRule() {
        // ARRANGE
        Event event = publishedEvent(18);
        User user = user(true, birthDateForAge(event, 25));
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(user));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
        when(ticketRepository.countByEventEventCodeAndStatus(eq(EVENT_CODE), eq(TicketStatus.PAID)))
                .thenReturn((long) CAPACITY);

        // ACT
        Throwable thrown = catchThrowable(() -> service.purchase(purchaseRequest(TicketType.GENERAL)));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-TICKET-009: cancelling a PAID ticket moves it to CANCELLED")
    void cancel_paidTicket_becomesCancelled() {
        // ARRANGE
        Ticket ticket = ticket(TicketStatus.PAID, publishedEvent(18));
        TicketResponse expected = ticketResponse(TicketStatus.CANCELLED);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(mapper.toResponse(ticket)).thenReturn(expected);

        // ACT
        TicketResponse result = service.cancel(TICKET_CODE);

        // ASSERT
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(result).isEqualTo(expected);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void cancel_missingTicket_throwsResourceNotFound() {
        // ARRANGE
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.cancel(TICKET_CODE));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(TICKET_CODE);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @ParameterizedTest
    @EnumSource(value = TicketStatus.class, names = {"USED", "CANCELLED"})
    @DisplayName("TEST-TICKET-010 / BR-TICKET-011: USED or CANCELLED ticket cannot be cancelled")
    void cancel_usedOrCancelledTicket_throwsBusinessRule(TicketStatus status) {
        // ARRANGE
        Ticket ticket = ticket(status, publishedEvent(18));
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

        // ACT
        Throwable thrown = catchThrowable(() -> service.cancel(TICKET_CODE));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(ticket.getStatus()).isEqualTo(status);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("BR-TICKET-012: PAID ticket cannot be cancelled after the event date")
    void cancel_afterEventDate_throwsBusinessRule() {
        // ARRANGE
        Event pastEvent = event(EventStatus.PUBLISHED, LocalDateTime.now().minusDays(1), 18);
        Ticket ticket = ticket(TicketStatus.PAID, pastEvent);
        when(ticketRepository.findByTicketCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

        // ACT
        Throwable thrown = catchThrowable(() -> service.cancel(TICKET_CODE));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository, never()).save(any(Ticket.class));
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

    private Ticket ticket(TicketStatus status, Event event) {
        return new Ticket(user(true, LocalDate.now().minusYears(30)), event, TICKET_CODE,
                TicketType.VIP, new BigDecimal("240000.00"), status, LocalDateTime.now().minusDays(1));
    }

    private TicketResponse ticketResponse(TicketStatus status) {
        return new TicketResponse(1L, TICKET_CODE, TicketType.VIP, new BigDecimal("240000.00"),
                status, LocalDateTime.now(), USER_EMAIL, EVENT_CODE, "Caribbean Music Fest 2026");
    }
}