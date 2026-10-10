package com.pulse.pass.controller;

import com.pulse.pass.dto.request.PurchaseTicketRequest;
import com.pulse.pass.dto.response.TicketResponse;
import com.pulse.pass.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    // FR-CTRL-TKT-001
    @PostMapping("/tickets")
    public ResponseEntity<TicketResponse> purchase(@Valid @RequestBody PurchaseTicketRequest request) {
        TicketResponse response = service.purchase(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // FR-CTRL-TKT-002
    @GetMapping("/tickets/{ticketCode}")
    public ResponseEntity<TicketResponse> findByCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok(service.findByCode(ticketCode));
    }

    // FR-CTRL-TKT-003
    @GetMapping("/tickets/by-user")
    public ResponseEntity<List<TicketResponse>> findByUserEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.findByUserEmail(email));
    }

    // FR-CTRL-TKT-004
    @GetMapping("/events/{eventCode}/tickets/paid")
    public ResponseEntity<List<TicketResponse>> findPaidTicketsByEvent(@PathVariable String eventCode) {
        return ResponseEntity.ok(service.findPaidTicketsByEvent(eventCode));
    }

    // FR-CTRL-TKT-005
    @PatchMapping("/tickets/{ticketCode}/cancel")
    public ResponseEntity<TicketResponse> cancel(@PathVariable String ticketCode) {
        return ResponseEntity.ok(service.cancel(ticketCode));
    }
}