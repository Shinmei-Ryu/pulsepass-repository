package com.pulse.pass.service;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.repository.TicketRepository;
import com.pulse.pass.repository.UserRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceImplTest {

    private static final Long ME = 1L;
    private static final Long OTHER = 2L;
    private static final String TICKET = "TCK-1";

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationServiceImpl authz;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("T-SEC-010: isSelf resolves the requested email to a persistent id and compares ids")
    void isSelf_emailOfAuthenticatedUser_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(userRepository.findByEmailIgnoreCase("ME@Email.com")).thenReturn(Optional.of(user(ME)));

        // ACT / ASSERT
        assertThat(authz.isSelf("ME@Email.com")).isTrue();
    }

    @Test
    @DisplayName("T-SEC-010: isSelf is false for another user's email")
    void isSelf_emailOfAnotherUser_returnsFalse() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(userRepository.findByEmailIgnoreCase("other@email.com")).thenReturn(Optional.of(user(OTHER)));

        // ACT / ASSERT
        assertThat(authz.isSelf("other@email.com")).isFalse();
    }

    @Test
    @DisplayName("T-SEC-010: isSelf is false for an unknown email, same answer as a foreign one")
    void isSelf_unknownEmail_returnsFalse() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(userRepository.findByEmailIgnoreCase("ghost@email.com")).thenReturn(Optional.empty());

        // ACT / ASSERT
        assertThat(authz.isSelf("ghost@email.com")).isFalse();
    }

    @Test
    @DisplayName("T-SEC-010: isSelf with null email is false without querying")
    void isSelf_nullEmail_returnsFalseWithoutQuery() {
        // ARRANGE
        authenticateAs(Role.USER);

        // ACT / ASSERT
        assertThat(authz.isSelf(null)).isFalse();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("T-SEC-015: ADMIN passes isSelfOrAdmin for any email without querying")
    void isSelfOrAdmin_admin_returnsTrueWithoutQuery() {
        // ARRANGE
        authenticateAs(Role.ADMIN);

        // ACT / ASSERT
        assertThat(authz.isSelfOrAdmin("other@email.com")).isTrue();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("T-SEC-015: STAFF is not ADMIN: foreign email is denied")
    void isSelfOrAdmin_staffForeignEmail_returnsFalse() {
        // ARRANGE
        authenticateAs(Role.STAFF);
        when(userRepository.findByEmailIgnoreCase("other@email.com")).thenReturn(Optional.of(user(OTHER)));

        // ACT / ASSERT
        assertThat(authz.isSelfOrAdmin("other@email.com")).isFalse();
    }

    @Test
    @DisplayName("T-SEC-015: USER may look up their own username, not another one")
    void isSelfByUsernameOrAdmin_userSelfOnly() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(userRepository.findByUsername("me")).thenReturn(Optional.of(user(ME)));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(user(OTHER)));

        // ACT / ASSERT
        assertThat(authz.isSelfByUsernameOrAdmin("me")).isTrue();
        assertThat(authz.isSelfByUsernameOrAdmin("other")).isFalse();
    }

    @Test
    @DisplayName("T-SEC-015: ADMIN may look up any username")
    void isSelfByUsernameOrAdmin_admin_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.ADMIN);

        // ACT / ASSERT
        assertThat(authz.isSelfByUsernameOrAdmin("other")).isTrue();
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("T-SEC-009: owner can view their ticket")
    void canViewTicket_owner_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(ME));

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("T-SEC-009: a foreign ticket is denied to a regular user")
    void canViewTicket_foreignTicket_returnsFalse() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(OTHER));

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isFalse();
    }

    @Test
    @DisplayName("T-SEC-009: STAFF can view any ticket without querying ownership")
    void canViewTicket_staff_returnsTrueWithoutQuery() {
        // ARRANGE
        authenticateAs(Role.STAFF);

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
        verifyNoInteractions(ticketRepository);
    }

    @Test
    @DisplayName("T-SEC-009: missing ticket passes authorization so the Service answers 404")
    void canViewTicket_missingTicket_returnsTrue() {
        // ARRANGE
        authenticateAs(Role.USER);
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.empty());

        // ACT / ASSERT
        assertThat(authz.canViewTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("T-SEC-011: owner can cancel; STAFF cannot cancel a foreign ticket; ADMIN can")
    void canCancelTicket_ownerAndAdminAllowedStaffDenied() {
        // ARRANGE
        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(ME));

        // ACT / ASSERT
        authenticateAs(Role.USER);
        assertThat(authz.canCancelTicket(TICKET)).isTrue();

        when(ticketRepository.findOwnerIdByTicketCode(TICKET)).thenReturn(Optional.of(OTHER));
        authenticateAs(Role.STAFF);
        assertThat(authz.canCancelTicket(TICKET)).isFalse();

        authenticateAs(Role.ADMIN);
        assertThat(authz.canCancelTicket(TICKET)).isTrue();
    }

    @Test
    @DisplayName("SEC-001: anonymous caller is denied everywhere")
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
    @DisplayName("SEC-001: empty security context is denied")
    void noAuthentication_isDenied() {
        // ACT / ASSERT
        assertThat(authz.isSelf("me@email.com")).isFalse();
        assertThat(authz.isAdmin()).isFalse();
    }

    private User user(Long id) {
        User user = new User("u" + id, "u" + id + "@email.com", true);
        user.setId(id);
        return user;
    }

    private void authenticateAs(Role role) {
        SecurityPrincipal principal = new SecurityPrincipal(
                ME, "me", "me@email.com", "hash", true, Set.of(role));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}