package com.pulse.pass.service.impl;

import com.pulse.pass.domain.Artist;
import com.pulse.pass.domain.Event;
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
import com.pulse.pass.service.EventService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper mapper;

    public EventServiceImpl(EventRepository eventRepository, VenueRepository venueRepository,
                            ArtistRepository artistRepository, EventMapper mapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.mapper = mapper;
    }

    // FR-SVC-003 / BR-EVENT-001..006
    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {

        // BR-EVENT-001
        if (eventRepository.existsByEventCode(request.eventCode())) {
            throw new DuplicateResourceException("Event code already exists: " + request.eventCode());
        }

        // BR-EVENT-002
        Venue venue = venueRepository.findByCode(request.venueCode())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + request.venueCode()));

        // BR-EVENT-003
        if (!venue.getActive()) {
            throw new BusinessRuleException("Cannot create an event for an inactive venue: " + request.venueCode());
        }

        // BR-EVENT-004
        if (!request.eventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Event date must be in the future.");
        }

        // BR-EVENT-006
        if (request.minimumAge() < 0) {
            throw new BusinessRuleException("Minimum age cannot be negative.");
        }

        // BR-EVENT-005: el estado inicial siempre es DRAFT, el request no lo controla
        Event event = new Event(
                request.eventCode(), request.name(), request.description(), request.category(),
                EventStatus.DRAFT, request.eventDate(), request.minimumAge()
        );
        event.setVenue(venue);

        Event saved = eventRepository.save(event);

        return mapper.toResponse(saved);
    }

    // FR-SVC-004
    @Override
    public EventResponse findByCode(String eventCode) {

        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));

        return mapper.toResponse(event);
    }

    // FR-SVC-005
    @Override
    public List<EventSummaryResponse> findPublishedEvents() {

        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream()
                .map(mapper::toSummary)
                .toList();
    }

    // FR-SVC-006 / BR-EVENT-007..009
    @Override
    @Transactional
    public EventResponse publish(String eventCode) {

        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));

        // BR-EVENT-007
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException("Only DRAFT events can be published: " + eventCode);
        }

        // BR-EVENT-008
        if (!event.getEventDate().isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Cannot publish an event with a past date: " + eventCode);
        }

        // BR-EVENT-009
        if (!event.getVenue().getActive()) {
            throw new BusinessRuleException("Cannot publish an event whose venue is inactive: " + eventCode);
        }

        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);

        return mapper.toResponse(saved);
    }

    // FR-SVC-007 / BR-EVENT-010..011
    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {

        Event event = eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventCode));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + artistId));

        // BR-EVENT-011
        if (event.getStatus() == EventStatus.CANCELLED || event.getStatus() == EventStatus.FINISHED) {
            throw new BusinessRuleException("Cannot add artists to a " + event.getStatus() + " event: " + eventCode);
        }

        // BR-EVENT-010
        if (event.getArtists().contains(artist)) {
            throw new BusinessRuleException("Artist already associated with event: " + eventCode);
        }

        event.addArtist(artist);
        Event saved = eventRepository.save(event);

        return mapper.toResponse(saved);
    }

    // FR-SVC-008
    @Override
    public List<EventSummaryResponse> findByArtist(String stageName) {

        return eventRepository.findByArtistStageName(stageName)
                .stream()
                .map(mapper::toSummary)
                .toList();
    }
}