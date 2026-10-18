package com.pulse.pass.service;

public interface AuthorizationService {

    boolean isStaffOrAdmin();

    boolean isAdmin();

    /** El email pedido es el del usuario autenticado. */
    boolean isSelf(String email);

    boolean isSelfOrAdmin(String email);

    boolean isSelfByUsernameOrAdmin(String username);

    /** Dueño del ticket, STAFF o ADMIN. Ticket inexistente: true (que el Service responda 404). */
    boolean canViewTicket(String ticketCode);

    /** Dueño del ticket o ADMIN. Ticket inexistente: true (404 lo decide el Service). */
    boolean canCancelTicket(String ticketCode);
}