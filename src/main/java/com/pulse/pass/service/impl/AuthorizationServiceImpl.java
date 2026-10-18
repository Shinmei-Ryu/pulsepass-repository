package com.pulse.pass.service.impl;

import com.pulse.pass.domain.User;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.security.SecurityPrincipal;
import com.pulse.pass.service.AuthorizationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("authz")
public class AuthorizationServiceImpl implements AuthorizationService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public AuthorizationServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean isStaffOrAdmin() {
        return hasAuthority("ROLE_STAFF") || hasAuthority("ROLE_ADMIN");
    }

    @Override
    public boolean isAdmin() {
        return hasAuthority("ROLE_ADMIN");
    }

    @Override
    public boolean isSelf(String email) {
        // El email del request solo sirve para localizar al usuario; la decisión se toma por id.
        // Un email inexistente da false (403), igual que uno ajeno: no permite enumerar cuentas.
        return principal()
                .map(p -> email != null && userRepository.findByEmailIgnoreCase(email)
                        .map(User::getId)
                        .filter(p.getId()::equals)
                        .isPresent())
                .orElse(false);
    }

    @Override
    public boolean isSelfOrAdmin(String email) {
        return isAdmin() || isSelf(email);
    }

    @Override
    public boolean isSelfByUsernameOrAdmin(String username) {
        return isAdmin() || principal()
                .map(p -> username != null && userRepository.findByUsername(username)
                        .map(User::getId)
                        .filter(p.getId()::equals)
                        .isPresent())
                .orElse(false);
    }

    @Override
    public boolean canViewTicket(String ticketCode) {
        return isStaffOrAdmin() || isOwnerOrMissing(ticketCode);
    }

    @Override
    public boolean canCancelTicket(String ticketCode) {
        return isAdmin() || isOwnerOrMissing(ticketCode);
    }

    // Compara por id persistente, nunca por datos del request.
    private boolean isOwnerOrMissing(String ticketCode) {
        Optional<SecurityPrincipal> current = principal();
        if (current.isEmpty()) {
            return false;
        }
        return ticketRepository.findOwnerIdByTicketCode(ticketCode)
                .map(ownerId -> ownerId.equals(current.get().getId()))
                .orElse(true);
    }

    private Optional<SecurityPrincipal> principal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof SecurityPrincipal p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> authority.equals(a.getAuthority()));
    }
}