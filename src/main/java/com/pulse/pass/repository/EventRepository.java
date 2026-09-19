package com.pulse.pass.repository;

import com.pulse.pass.domain.Event;
import com.pulse.pass.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // ---------- Query Methods ----------

    // AC-002: buscar evento por eventCode
    Optional<Event> findByEventCode(String eventCode);

    // FR-EVT-005 / UC-06: eventos por estado (PUBLISHED) ordenados por fecha ascendente
    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    // FR-VEN-004: eventos de un venue navegando la relación venue.code
    List<Event> findByVenue_Code(String venueCode);

    // ---------- JPQL ----------

    // FR-ART-004 / FR-SRC-001 / UC-07: eventos en los que participa un artista (N:M, sin duplicados)
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.artists a
            WHERE a.stageName = :stageName
            """)
    List<Event> findByArtistStageName(@Param("stageName") String stageName);

    // FR-SRC-002: eventos de una ciudad en los que participa un artista específico
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE v.city = :city
              AND a.stageName = :stageName
            """)
    List<Event> findByCityAndArtistStageName(@Param("city") String city,
                                             @Param("stageName") String stageName);

    // FR-SRC-003 / UC-09: eventos publicados posteriores a una fecha, en una ciudad,
    // cuyo artista contenga un texto (case-insensitive), sin duplicados y ordenados por fecha
    @Query("""
            SELECT DISTINCT e
            FROM Event e
            JOIN e.venue v
            JOIN e.artists a
            WHERE e.status = com.pulsepass.domain.EventStatus.PUBLISHED
              AND e.eventDate > :date
              AND v.city = :city
              AND LOWER(a.stageName) LIKE LOWER(CONCAT('%', :artistText, '%'))
            ORDER BY e.eventDate ASC
            """)
    List<Event> findRecommendedEvents(@Param("date") LocalDateTime date,
                                      @Param("city") String city,
                                      @Param("artistText") String artistText);
}
