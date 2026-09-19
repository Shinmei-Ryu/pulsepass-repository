package com.pulse.pass.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "ticket_code", unique = true, nullable = false, length = 100)
    private String ticketCode;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TicketType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(name = "purchase_date", nullable = false)
    private LocalDateTime purchaseDate;

    public Ticket() {
    }

    public Ticket(User user, Event event, String ticketCode, TicketType type,
                  BigDecimal price, TicketStatus status, LocalDateTime purchaseDate) {
        this.user = user;
        this.event = event;
        this.ticketCode = ticketCode;
        this.type = type;
        this.price = price;
        this.status = status;
        this.purchaseDate = purchaseDate;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Event getEvent() {
        return event;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public TicketType getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

}
