package com.pulse.pass.service.impl;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventStatus;
import com.pulse.pass.domain.Ticket;
import com.pulse.pass.domain.TicketStatus;
import com.pulse.pass.domain.TicketType;
import com.pulse.pass.domain.User;
import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.TicketMapper;
import com.pulse.pass.repository.EventRepository;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.service.TicketService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    private static final BigDecimal GENERAL_BASE_PRICE = new BigDecimal("120000.00");
    private static final BigDecimal STUDENT_DISCOUNT = new BigDecimal("0.50");
    private static final BigDecimal VIP_MULTIPLIER = new BigDecimal("2.00");
    private static final BigDecimal BACKSTAGE_MULTIPLIER = new BigDecimal("3.00");

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;
    private final TicketMapper mapper;

    public TicketServiceImpl(UserRepository userRepository, EventRepository eventRepository,
                             TicketRepository ticketRepository, TicketMapper mapper) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketRepository = ticketRepository;
        this.mapper = mapper;
    }

    // FR-SVC-013 / BR-TICKET-001..009
    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {

        // BR-TICKET-001
        User user = userRepository.findByEmailIgnoreCase(request.userEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.userEmail()));

        // BR-TICKET-002
        if (!user.getActive()) {
            throw new BusinessRuleException("Inactive user cannot purchase tickets: " + request.userEmail());
        }

        // BR-TICKET-003
        Event event = eventRepository.findByEventCode(request.eventCode())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + request.eventCode()));

        // BR-TICKET-004
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException("Cannot purchase tickets for a " + event.getStatus() + " event.");
        }

        // BR-TICKET-005
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot purchase tickets for a past event.");
        }

        // BR-TICKET-006
        if (event.getMinimumAge() > 0) {
            int ageAtEvent = Period.between(
                    user.getUserProfile().getBirthDate(),
                    event.getEventDate().toLocalDate()
            ).getYears();

            if (ageAtEvent < event.getMinimumAge()) {
                throw new BusinessRuleException("User does not meet minimum age for event: " + request.eventCode());
            }
        }

        // BR-TICKET-007
        long paidTickets = ticketRepository.countByEventEventCodeAndStatus(request.eventCode(), TicketStatus.PAID);

        if (paidTickets >= event.getVenue().getCapacity()) {
            throw new BusinessRuleException("No capacity available for event: " + request.eventCode());
        }

        // BR-TICKET-009: la estrategia garantiza price >= 0
        BigDecimal price = calculatePrice(request.type());

        Ticket ticket = new Ticket(
                user, event, generateTicketCode(event), request.type(),
                price, TicketStatus.PAID, LocalDateTime.now()
        );
        Ticket savedTicket = ticketRepository.save(ticket);

        // BR-TICKET-008
        if (paidTickets + 1 == event.getVenue().getCapacity()) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return mapper.toResponse(savedTicket);
    }

    // Estrategia de precio encapsulada (sección 28) — GENERAL: base, STUDENT: descuento,
    // VIP/BACKSTAGE: multiplicador. BR-TICKET-009: nunca negativo por construcción.
    private BigDecimal calculatePrice(TicketType type) {
        return switch (type) {
            case GENERAL -> GENERAL_BASE_PRICE;
            case STUDENT -> GENERAL_BASE_PRICE.multiply(BigDecimal.ONE.subtract(STUDENT_DISCOUNT));
            case VIP -> GENERAL_BASE_PRICE.multiply(VIP_MULTIPLIER);
            case BACKSTAGE -> GENERAL_BASE_PRICE.multiply(BACKSTAGE_MULTIPLIER);
        };
    }

    private String generateTicketCode(Event event) {
        return "TCK-" + event.getEventCode() + "-" + System.currentTimeMillis();
    }

    // FR-SVC-014
    @Override
    public TicketResponse findByCode(String ticketCode) {

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));

        return mapper.toResponse(ticket);
    }

    // FR-SVC-015
    @Override
    public List<TicketResponse> findByUserEmail(String email) {

        return ticketRepository.findByUserEmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // FR-SVC-016
    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {

        return ticketRepository.findByEvent_EventCodeAndStatus(eventCode, TicketStatus.PAID)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // FR-SVC-017 / BR-TICKET-010..012
    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));

        // BR-TICKET-010 / BR-TICKET-011
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be cancelled: " + ticketCode);
        }

        // BR-TICKET-012
        if (ticket.getEvent().getEventDate().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot cancel a ticket after the event date: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toResponse(saved);
    }

    // FR-SVC-018 / BR-TICKET-013..014
    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {

        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found: " + ticketCode));

        // BR-TICKET-013 / BR-TICKET-014
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException("Only PAID tickets can be marked as used: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.USED);
        Ticket saved = ticketRepository.save(ticket);

        return mapper.toResponse(saved);
    }
}