package com.pulse.pass.repository;

import com.pulse.pass.domain.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    // FR-ART-001 / FR-ART-002: recuperar artista por nombre artístico
    Optional<Artist> findByStageName(String stageName);
}
