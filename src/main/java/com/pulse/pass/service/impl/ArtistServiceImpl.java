package com.pulse.pass.service.impl;

import com.pulse.pass.domain.Artist;
import com.pulse.pass.dto.response.ArtistResponse;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.ArtistMapper;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.service.ArtistService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository repository;
    private final ArtistMapper mapper;

    public ArtistServiceImpl(ArtistRepository repository, ArtistMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    // FR-SVC-009 / BR-ARTIST-001
    @Override
    public ArtistResponse findById(Long id) {

        Artist artist = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + id));

        return mapper.toResponse(artist);
    }

    // FR-SVC-009 / BR-ARTIST-001
    @Override
    public ArtistResponse findByStageName(String stageName) {

        Artist artist = repository.findByStageNameIgnoreCase(stageName)
                .orElseThrow(() -> new ResourceNotFoundException("Artist not found: " + stageName));

        return mapper.toResponse(artist);
    }

    // BR-ARTIST-002
    @Override
    public List<ArtistResponse> findActiveArtists() {

        return repository.findByActiveTrueOrderByStageNameAsc()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}