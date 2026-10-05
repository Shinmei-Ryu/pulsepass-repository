package com.pulse.pass.service.impl;

import com.pulse.pass.domain.Venue;
import com.pulse.pass.dto.response.VenueResponse;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.VenueMapper;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.VenueService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {

    private final VenueRepository repository;
    private final VenueMapper mapper;

    public VenueServiceImpl(VenueRepository repository, VenueMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // FR-SVC-001 / BR-VENUE-001
    @Override
    public VenueResponse findByCode(String code) {

        Venue venue = repository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found: " + code));

        return mapper.toResponse(venue);
    }

    // FR-SVC-002 / BR-VENUE-002
    @Override
    public List<VenueResponse> findActiveVenues() {

        return repository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}