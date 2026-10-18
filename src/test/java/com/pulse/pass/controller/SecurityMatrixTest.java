package com.pulse.pass.controller;

import com.pulse.pass.domain.Role;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.security.CustomUserDetailsService;
import com.pulse.pass.security.InvalidJwtException;
import com.pulse.pass.security.JwtService;
import com.pulse.pass.security.SecurityConfig;
import com.pulse.pass.security.SecurityPrincipal;
import com.pulse.pass.service.ArtistService;
import com.pulse.pass.service.AuthService;
import com.pulse.pass.service.EventService;
import com.pulse.pass.service.TicketService;
import com.pulse.pass.service.UserService;
import com.pulse.pass.service.VenueService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        VenueController.class, EventController.class, ArtistController.class,
        UserController.class, TicketController.class, AuthController.class
})
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class SecurityMatrixTest {

    enum Identity { ANONYMOUS, USER, STAFF, ADMIN }

    // Quién puede usar cada ruta: es la matriz del Anexo C
    private static final Set<Identity> PUBLIC = EnumSet.allOf(Identity.class);
    private static final Set<Identity> AUTHENTICATED = EnumSet.of(Identity.USER, Identity.STAFF, Identity.ADMIN);
    private static final Set<Identity> USER_ONLY = EnumSet.of(Identity.USER);
    private static final Set<Identity> STAFF_OR_ADMIN = EnumSet.of(Identity.STAFF, Identity.ADMIN);
    private static final Set<Identity> ADMIN_ONLY = EnumSet.of(Identity.ADMIN);

    record Route(String label, HttpMethod method, String uri, String body, Set<Identity> allowed) {
        @Override
        public String toString() {
            return label;
        }
    }

    private static final String EVENT_BODY = """
            {"eventCode":"CMF-2026","name":"Caribbean Music Fest 2026","description":"Festival",
             "category":"MUSIC","eventDate":"2026-12-01T20:00:00","minimumAge":18,"venueCode":"VEN-SMR-01"}
            """;

    private static final String USER_BODY = """
            {"username":"andrea","email":"andrea@email.com","password":"UnaFraseSeguraLarga2026!",
             "firstName":"Andrea","lastName":"Lopez","phone":"3001234567","city":"Santa Marta",
             "birthDate":"2000-05-10"}
            """;

    private static final String PURCHASE_BODY = """
            {"userEmail":"andrea@email.com","eventCode":"CMF-2026","type":"VIP"}
            """;

    private static final String LOGIN_BODY = """
            {"identifier":"andrea","password":"UnaFraseSeguraLarga2026!"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VenueService venueService;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private ArtistService artistService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    // ------------------------------------------------------------------
    // Matriz completa: 22 rutas x 4 identidades
    // ------------------------------------------------------------------

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("cases")
    @DisplayName("T-SEC-001..004, 012..014: every route follows the role matrix")
    void routeAccessFollowsTheMatrix(Route route, Identity identity) throws Exception {
        // ARRANGE
        MockHttpServletRequestBuilder request = request(route.method(), route.uri());
        if (route.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(route.body());
        }
        if (identity != Identity.ANONYMOUS) {
            request.with(user("tester").roles(identity.name()));
        }

        // ACT
        int status = mockMvc.perform(request).andReturn().getResponse().getStatus();

        // ASSERT
        if (route.allowed().contains(identity)) {
            assertThat(status).isNotIn(401, 403);
        } else {
            assertThat(status).isEqualTo(identity == Identity.ANONYMOUS ? 401 : 403);
            verifyNoInteractions(venueService, eventService, artistService,
                    userService, ticketService, authService);
        }
    }

    static Stream<Arguments> cases() {
        return routes().stream().flatMap(route ->
                Arrays.stream(Identity.values()).map(identity -> Arguments.of(route, identity)));
    }

    static List<Route> routes() {
        return List.of(
                new Route("GET /api/venues/{code}", HttpMethod.GET, "/api/venues/VEN-SMR-01", null, PUBLIC),
                new Route("GET /api/venues/active", HttpMethod.GET, "/api/venues/active", null, PUBLIC),

                new Route("POST /api/events", HttpMethod.POST, "/api/events", EVENT_BODY, ADMIN_ONLY),
                new Route("GET /api/events/{eventCode}", HttpMethod.GET, "/api/events/CMF-2026", null, PUBLIC),
                new Route("GET /api/events/published", HttpMethod.GET, "/api/events/published", null, PUBLIC),
                new Route("PATCH /api/events/{eventCode}/publish", HttpMethod.PATCH,
                        "/api/events/CMF-2026/publish", null, ADMIN_ONLY),
                new Route("POST /api/events/{eventCode}/artists/{artistId}", HttpMethod.POST,
                        "/api/events/CMF-2026/artists/1", null, ADMIN_ONLY),
                new Route("GET /api/events/by-artist", HttpMethod.GET,
                        "/api/events/by-artist?stageName=Solar", null, PUBLIC),

                new Route("GET /api/artists/{id}", HttpMethod.GET, "/api/artists/1", null, PUBLIC),
                new Route("GET /api/artists/by-stage-name", HttpMethod.GET,
                        "/api/artists/by-stage-name?stageName=Solar", null, PUBLIC),
                new Route("GET /api/artists/active", HttpMethod.GET, "/api/artists/active", null, PUBLIC),

                new Route("POST /api/users", HttpMethod.POST, "/api/users", USER_BODY, PUBLIC),
                new Route("GET /api/users/by-email", HttpMethod.GET,
                        "/api/users/by-email?email=andrea@email.com", null, AUTHENTICATED),
                new Route("GET /api/users/by-username", HttpMethod.GET,
                        "/api/users/by-username?username=andrea", null, AUTHENTICATED),

                new Route("POST /api/tickets", HttpMethod.POST, "/api/tickets", PURCHASE_BODY, USER_ONLY),
                new Route("GET /api/tickets/{ticketCode}", HttpMethod.GET, "/api/tickets/TCK-1", null, AUTHENTICATED),
                new Route("GET /api/tickets/by-user", HttpMethod.GET,
                        "/api/tickets/by-user?email=andrea@email.com", null, AUTHENTICATED),
                new Route("GET /api/events/{eventCode}/tickets/paid", HttpMethod.GET,
                        "/api/events/CMF-2026/tickets/paid", null, STAFF_OR_ADMIN),
                new Route("PATCH /api/tickets/{ticketCode}/cancel", HttpMethod.PATCH,
                        "/api/tickets/TCK-1/cancel", null, AUTHENTICATED),
                new Route("PATCH /api/tickets/{ticketCode}/use", HttpMethod.PATCH,
                        "/api/tickets/TCK-1/use", null, STAFF_OR_ADMIN),

                new Route("POST /api/auth/login", HttpMethod.POST, "/api/auth/login", LOGIN_BODY, PUBLIC),
                new Route("GET /api/auth/me", HttpMethod.GET, "/api/auth/me", null, AUTHENTICATED)
        );
    }

    // ------------------------------------------------------------------
    // Comportamientos puntuales
    // ------------------------------------------------------------------

    @Test
    @DisplayName("T-SEC-018: authorization is evaluated before validation")
    void authorizationIsEvaluatedBeforeValidation() throws Exception {
        // Sin el rol: 403, aunque el cuerpo sea inválido; el Service no se toca
        mockMvc.perform(post("/api/events")
                        .with(user("tester").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        // Con el rol: la validación responde 400, no 403
        mockMvc.perform(post("/api/events")
                        .with(user("tester").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request validation failed"));

        verifyNoInteractions(eventService);
    }

    @Test
    @DisplayName("T-SEC-019: 403 uses the ErrorResponse contract without internal details")
    void forbiddenResponseUsesErrorResponseContract() throws Exception {
        mockMvc.perform(patch("/api/events/CMF-2026/publish").with(user("tester").roles("USER")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied"))
                .andExpect(jsonPath("$.details").isMap())
                .andExpect(jsonPath("$.length()").value(5));
    }

    @Test
    @DisplayName("T-SEC-007: 401 uses the ErrorResponse contract and challenges with Bearer")
    void unauthenticatedResponseChallengesWithBearer() throws Exception {
        mockMvc.perform(get("/api/tickets/TCK-1"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required"))
                .andExpect(jsonPath("$.length()").value(5));

        verifyNoInteractions(ticketService);
    }

    @Test
    @DisplayName("SEC deny-by-default: a route that is not listed requires authentication")
    void unlistedRouteIsDeniedByDefault() throws Exception {
        mockMvc.perform(delete("/api/venues/VEN-SMR-01"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(venueService);
    }

    @Test
    @DisplayName("An invalid Bearer token is rejected even on a public route")
    void invalidBearerTokenOnPublicRouteIsRejected() throws Exception {
        when(jwtService.validateAndExtractUserId("bad-token")).thenThrow(new InvalidJwtException());

        mockMvc.perform(get("/api/venues/active").header("Authorization", "Bearer bad-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired token"));

        verifyNoInteractions(venueService);
    }

    @Test
    @DisplayName("SEC-011: roles come from the freshly loaded principal, so an ADMIN token reaches an ADMIN route")
    void bearerTokenOfAdminReachesAdminRoute() throws Exception {
        authenticateTokenAs("admin-token", Role.ADMIN);

        mockMvc.perform(patch("/api/events/CMF-2026/publish").header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("SEC-011: a USER token is forbidden on an ADMIN route")
    void bearerTokenOfUserIsForbiddenOnAdminRoute() throws Exception {
        authenticateTokenAs("user-token", Role.USER);

        mockMvc.perform(patch("/api/events/CMF-2026/publish").header("Authorization", "Bearer user-token"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));

        verifyNoInteractions(eventService);
    }

    private void authenticateTokenAs(String token, Role role) {
        when(jwtService.validateAndExtractUserId(token)).thenReturn(1L);
        when(userDetailsService.loadPrincipalById(1L))
                .thenReturn(new SecurityPrincipal(1L, "tester", "tester@email.com", null, true, Set.of(role)));
    }
}