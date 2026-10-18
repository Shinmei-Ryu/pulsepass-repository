package com.pulse.pass.service;

import com.pulse.pass.domain.Role;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.security.SecurityPrincipal;
import com.pulse.pass.service.impl.AuthorizationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    private static final Long ME = 1L;
    private static final Long OTHER = 2L;
    private static final String TICKET = "TCK-1";

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private AuthorizationServiceImpl authz;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("T-SEC-009: isSelf compares against the authenticated email, ignoring case")
    void isSelf_sameEmailDifferentCase_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);

        // ACT / ASSERT
        assertThat(authz.isSelf("ME@Email.com")).isTrue();
        assertThat(authz.isSelf("other@email.com")).isFalse();
        assertThat(authz.isSelf(null)).isFalse();
    }

    @Test
    @DisplayName("T-SEC-011: ADMIN passes isSelfOrAdmin for any email; USER and STAFF do not")
    void isSelfOrAdmin_onlyAdminOrSelf() {
        // ARRANGE / ACT / ASSERT
        authenticateAs(Role.ADMIN);
        assertThat(authz.isSelfOrAdmin("other@email.com")).isTrue();

        authenticateAs(Role.STAFF);
        assertThat(authz.isSelfOrAdmin("other@email.com")).isFalse();

        authenticateAs(Role.USER);
        assertThat(authz.isSelfOrAdmin("other@email.com")).isFalse();
        assertThat(authz.isSelfOrAdmin("me@email.com")).isTrue();
    }

    @Test
    @DisplayName("T-SEC-017: username access is self or ADMIN")
    void isSelfByUsernameOrAdmin_selfOrAdminOnly() {
        // ARRANGE / ACT / ASSERT
        authenticateAs(Role.USER);
        assertThat(authz.isSelfByUsernameOrAdmin("me")).isTrue();
        assertThat(authz.isSelfByUsernameOrAdmin("other")).isFalse();

        authenticateAs(Role.ADMIN);
        assertThat(authz.isSelfByUsernameOrAdmin("other")).isTrue();
    }

    @Test
    @DisplayName("T-SEC-010: owner can view their ticket")
    void canViewTicket_owner_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(ME));

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("T-SEC-010: a foreign ticket is denied to a regular user")
    void canViewTicket_foreignTicket_returnsFalse() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(OTHER));

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isFalse();
    }

    @Test
    @DisplayName("T-SEC-010: STAFF can view any ticket without querying ownership")
    void canViewTicket_staff_returnsTrueWithoutQuery() {
        // ARRANGE
        authenticateAs(Role.STAFF);

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
        org.mockito.Mockito.verifyNoInteractions(ticketRepository);
    }

    @Test
    @DisplayName("T-SEC-010: missing ticket passes authorization so the Service answers 404")
    void canViewTicket_missingTicket_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.empty());

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("T-SEC-010: STAFF cannot cancel a foreign ticket, ADMIN can")
    void canCancelTicket_staffDeniedAdminAllowed() {
        // ARRANGE
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(OTHER));

        // ACT / ASSERT
        authenticateAs(Role.STAFF);
        assertThat(authz.canCancelTicket(TICKET)).isFalse();

        authenticateAs(Role.ADMIN);
        assertThat(authz.canCancelTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("T-SEC-009: anonymous caller is denied everywhere")
    void anonymous_isDenied() {
        // ARRANGE
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        // ACT / ASSERT
        assertThat(authz.isSelf("me@email.com")).isFalse();
        assertThat(authz.isStaffOrAdmin()).isFalse();
        assertThat(authz.canViewTicket(TICKET)).isFalse();
    }

    @Test
    @DisplayName("T-SEC-009: empty context is denied")
    void noAuthentication_isDenied() {
        // ACT / ASSERT
        assertThat(authz.isSelf("me@email.com")).isFalse();
        assertThat(authz.isAdmin()).isFalse();
    }

    private void authenticateAs(Role role) {
        SecurityPrincipal principal = new SecurityPrincipal(
                ME, "me", "me@email.com", "hash", true, Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}