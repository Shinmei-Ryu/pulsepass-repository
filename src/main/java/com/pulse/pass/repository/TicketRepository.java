package com.pulse.pass.repository;

import com.pulse.pass.domain.Ticket;
import com.pulse.pass.domain.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ---------- Query Methods ----------

    // FR-TKT-002 / AC-005: recuperar ticket por código
    Optional<Ticket> findByTicketCode(String ticketCode);

    // FR-TKT-006: tickets de un usuario por email (Ticket -> User -> email)
    List<Ticket> findByUser_Email(String email);

    // FR-TKT-006: tickets de un usuario por email y estado
    List<Ticket> findByUser_EmailAndStatus(String email, TicketStatus status);

    // FR-TKT-007 / UC-08: tickets de un evento por eventCode y estado (usar PAID).
    // Query Method: un solo filtro navegando una relación, no justifica JPQL.
    List<Ticket> findByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-SRC-004: tickets de eventos posteriores a una fecha, ordenados cronológicamente
    List<Ticket> findByEvent_EventDateAfterOrderByEvent_EventDateAsc(LocalDateTime date);

    // ---------- JPQL ----------

    // FR-TKT-008 / AC-008: conteo de tickets PAID de un evento
    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            JOIN t.event e
            WHERE e.eventCode = :eventCode
              AND t.status = com.pulse.pass.domain.TicketStatus.PAID
            """)
    long countPaidByEventCode(@Param("eventCode") String eventCode);
}
