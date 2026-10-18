package com.pulse.pass.security;

import com.pulse.pass.domain.Role;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthenticationEntryPoint entryPoint;

    @Mock
    private FilterChain chain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_continuesAnonymously() throws Exception {
        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    @Test
    void nonBearerScheme_isIgnored() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Basic YWJjOmRlZg==");

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    @Test
    @DisplayName("FR-SEC-AUTH-007: a token in the query string is never read")
    void tokenInQueryString_isIgnored() throws Exception {
        // ARRANGE
        request.setParameter("access_token", "some-token");

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    @Test
    @DisplayName("FR-SEC-AUTH-009: valid Bearer authenticates with a principal freshly loaded from the database")
    void validBearer_authenticatesWithFreshPrincipal() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer good-token");
        SecurityPrincipal principal = principal(true);
        when(jwtService.validateAndExtractUserId("good-token")).thenReturn(42L);
        when(userDetailsService.loadPrincipalById(42L)).thenReturn(principal);

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isSameAs(principal);
        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
        verify(chain).doFilter(request, response);
        verifyNoInteractions(entryPoint);
    }

    @Test
    @DisplayName("T-SEC-008: invalid token is rejected with 401 and the chain is not continued")
    void invalidToken_isRejected() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer bad-token");
        when(jwtService.validateAndExtractUserId("bad-token")).thenThrow(new InvalidJwtException());

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(entryPoint).commence(eq(request), eq(response), any(InvalidJwtException.class));
        verifyNoInteractions(chain, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("T-SEC-016 / FR-SEC-AUTH-004: valid token of an inactive user is rejected")
    void validTokenOfInactiveUser_isRejected() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer good-token");
        when(jwtService.validateAndExtractUserId("good-token")).thenReturn(42L);
        when(userDetailsService.loadPrincipalById(42L)).thenReturn(principal(false));

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(entryPoint).commence(eq(request), eq(response), any(InvalidJwtException.class));
        verifyNoInteractions(chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void validTokenOfDeletedUser_isRejected() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer good-token");
        when(jwtService.validateAndExtractUserId("good-token")).thenReturn(42L);
        when(userDetailsService.loadPrincipalById(42L)).thenThrow(new UsernameNotFoundException("User not found"));

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(entryPoint).commence(eq(request), eq(response), any(InvalidJwtException.class));
        verifyNoInteractions(chain);
    }

    @Test
    void emptyBearerToken_isRejectedWithoutCallingJwtService() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer ");

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(entryPoint).commence(eq(request), eq(response), any(InvalidJwtException.class));
        verifyNoInteractions(jwtService, chain);
    }

    @Test
    void multipleAuthorizationHeaders_areRejected() throws Exception {
        // ARRANGE
        request.addHeader("Authorization", "Bearer first-token");
        request.addHeader("Authorization", "Bearer second-token");

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(entryPoint).commence(eq(request), eq(response), any(InvalidJwtException.class));
        verifyNoInteractions(jwtService, chain);
    }

    @Test
    void existingAuthentication_isNeverOverridden() throws Exception {
        // ARRANGE
        Authentication existing = new TestingAuthenticationToken("someone", "n/a", "ROLE_ADMIN");
        SecurityContextHolder.getContext().setAuthentication(existing);
        request.addHeader("Authorization", "Bearer good-token");

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        verify(chain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existing);
        verifyNoInteractions(jwtService, userDetailsService, entryPoint);
    }

    private SecurityPrincipal principal(boolean active) {
        return new SecurityPrincipal(42L, "andrea", "andrea@email.com", null, active, Set.of(Role.USER));
    }
}