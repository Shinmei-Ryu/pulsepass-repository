package com.pulse.pass.repository;

import com.pulse.pass.domain.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    // FR-VEN-001 / UC-01: recuperar venue por código de negocio
    Optional<Venue> findByCode(String code);
}
