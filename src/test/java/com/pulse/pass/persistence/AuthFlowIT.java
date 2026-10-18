package com.pulse.pass.persistence;

import com.jayway.jsonpath.JsonPath;
import com.pulse.pass.domain.*;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
import com.pulse.pass.repository.ArtistRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.repository.UserRoleRepository;
import com.pulse.pass.repository.VenueRepository;
import com.pulse.pass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthFlowIT {

    private static final String PASSWORD = "UnaFraseSeguraLarga2026!";
    private static final String EVENT_CODE = "CMF-2026";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName("pulsepass_test")
                    .withUsername("pulsepass")
                    .withPassword("pulsepass");

    @Autowired private MockMvc mockMvc;
    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private ArtistRepository artistRepository;

    @Test
    @DisplayName("PRD §23: full demo scenario, steps 1-10")
    void fullDemoScenario() throws Exception {
        // Contexto heredado: venue, artista y cuentas de operación (solo demo)
        venueRepository.saveAndFlush(new Venue("VEN-SMR-01", "Marina Convention Center",
                "Santa Marta", "Calle 1 #1-01", 50, true));
        Long solarBeatId = artistRepository.findAll().stream()
                .filter(artist -> "Solar Beat".equals(artist.getStageName()))
                .findFirst()
                .orElseGet(() -> artistRepository.saveAndFlush(
                        new Artist("Solar Beat", "Colombia", "Pop", true)))
                .getId();
        createPrivilegedAccount("admin.demo", Role.ADMIN);
        createPrivilegedAccount("laura.staff", Role.STAFF);
        String adminToken = login("admin.demo");
        String lauraToken = login("laura.staff");

        // 1. Andrea se registra con contraseña; la respuesta no trae hash ni contraseña
        MvcResult registered = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("andrea")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn();
        assertThat(registered.getResponse().getContentAsString()).doesNotContain(PASSWORD);
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("carlos")))
                .andExpect(status().isCreated());

        // 2. Andrea inicia sesión: Bearer con expiresIn = 900, sin cookie de sesión (FR-SEC-AUTH-008)
        MvcResult loggedIn = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("andrea", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(header().exists("X-Request-Id"))
                .andReturn();
        String andreaToken = JsonPath.read(loggedIn.getResponse().getContentAsString(), "$.accessToken");
        String carlosToken = login("carlos");

        // El admin crea CMF-2026: nace en borrador y un anónimo no lo ve (T-SEC-017)
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createEventBody()))
                .andExpect(status().isCreated());
        mockMvc.perform(get("/api/events/{code}", EVENT_CODE))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventCode", not(hasItem(EVENT_CODE))));

        // 6. Andrea intenta publicar: 403
        mockMvc.perform(patch("/api/events/{code}/publish", EVENT_CODE)
                        .header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isForbidden());

        // 7. El administrador publica y asocia a Solar Beat con las rutas existentes
        mockMvc.perform(patch("/api/events/{code}/publish", EVENT_CODE)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(post("/api/events/{code}/artists/{id}", EVENT_CODE, solarBeatId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // 3. Un anónimo encuentra CMF-2026 entre los publicados
        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].eventCode", hasItem(EVENT_CODE)));

        // 4. Andrea compra su propia entrada
        String andreaTicket = buyTicket(andreaToken, "andrea@email.com");
        String carlosTicket = buyTicket(carlosToken, "carlos@email.com");

        // 5. Andrea intenta consultar o cancelar la entrada de Carlos: acceso denegado
        mockMvc.perform(get("/api/tickets/{code}", carlosTicket)
                        .header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/tickets/{code}/cancel", carlosTicket)
                        .header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isForbidden());

        // T-SEC-012: un USER no puede marcar uso, ni siquiera de su propio ticket
        mockMvc.perform(patch("/api/tickets/{code}/use", andreaTicket)
                        .header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isForbidden());

        // 8. Laura (STAFF) marca la entrada como usada; la segunda vez la regla heredada responde 409
        mockMvc.perform(patch("/api/tickets/{code}/use", andreaTicket)
                        .header("Authorization", "Bearer " + lauraToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));
        mockMvc.perform(patch("/api/tickets/{code}/use", andreaTicket)
                        .header("Authorization", "Bearer " + lauraToken))
                .andExpect(status().isConflict());

        // 9. Se altera un carácter del payload del JWT: 401
        String[] parts = andreaToken.split("\\.");
        char original = parts[1].charAt(5);
        char changed = original == 'A' ? 'B' : 'A';
        String tampered = parts[0] + "." + parts[1].substring(0, 5) + changed
                + parts[1].substring(6) + "." + parts[2];
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());

        // 10. Se desactiva a Andrea con su token aún vigente: el siguiente request no es autorizado
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isOk());
        User andrea = userRepository.findByUsername("andrea").orElseThrow();
        andrea.setActive(false);
        userRepository.saveAndFlush(andrea);
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + andreaToken))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Bearer",
            "Bearer ",
            "Bearer x",
            "Bearer a.b.c",
            "Bearer eyJhbGciOiJub25lIn0.e30.",
            "Basic YWJjOmRlZg=="
    })
    @DisplayName("NFR-SEC-002 / FR-SEC-AUTH-006: malformed or unexpected credentials give 401, never 500")
    void malformedAuthorizationHeaders_return401(String header) throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", header))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("NFR-SEC-002: an oversized Bearer token gives 401, never 500")
    void oversizedBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + "A".repeat(8192)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("FR-SEC-AUTH-003 / T-SEC-019: wrong password and unknown user are indistinguishable, with no internals")
    void loginFailures_areGenericAndLeakNothing() throws Exception {
        registerOnly("generic.user");

        String wrongPassword = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("generic.user", "OtraFraseIncorrecta2026!")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        String unknownUser = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("nobody.here", PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();

        assertThat((String) JsonPath.read(wrongPassword, "$.message"))
                .isEqualTo(JsonPath.read(unknownUser, "$.message"));
        assertThat(wrongPassword.toLowerCase())
                .doesNotContain("stacktrace", "exception", "bcrypt", "hash", PASSWORD.toLowerCase());
    }

    @Test
    @DisplayName("DoD: ErrorResponse keeps one contract for 400, 403, 404 and 409; authorization precedes validation")
    void errorContractIsConsistent() throws Exception {
        createPrivilegedAccount("admin.err", Role.ADMIN);
        String adminToken = login("admin.err");
        registerOnly("user.err");
        String userToken = login("user.err");
        String invalidEventBody = """
                {"eventCode": "", "name": "", "category": "MUSIC", "eventDate": "%s",
                 "minimumAge": 18, "venueCode": "VEN-NONE"}
                """.formatted(LocalDateTime.now().plusDays(30));

        // 400: ADMIN autorizado con cuerpo inválido (T-SEC-018)
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.details").exists());

        // 403: el mismo cuerpo inválido enviado por un USER se corta antes de validar
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.timestamp").exists());

        // 404
        mockMvc.perform(get("/api/events/{code}", "NOPE-0000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.timestamp").exists());

        // 409: registro duplicado
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody("user.err")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ---------------- helpers ----------------

    private void registerOnly(String username) {
        userService.register(registerRequest(username));
    }

    private void createPrivilegedAccount(String username, Role role) {
        UserResponse created = userService.register(registerRequest(username));
        // El registro público siempre da USER; solo las cuentas de demostración se promueven aquí
        userRoleRepository.deleteById(new UserRoleId(created.id(), Role.USER));
        User user = userRepository.findById(created.id()).orElseThrow();
        userRoleRepository.saveAndFlush(new UserRole(user, role));
    }

    private RegisterUserRequest registerRequest(String username) {
        return new RegisterUserRequest(username, username + "@email.com", PASSWORD,
                "Demo", "Account", "3001234567", "Santa Marta", LocalDate.of(1995, 4, 12));
    }

    private String registerBody(String username) {
        return """
                {"username": "%s", "email": "%s@email.com", "password": "%s",
                 "firstName": "Demo", "lastName": "Account", "phone": "3001234567",
                 "city": "Santa Marta", "birthDate": "1995-04-12"}
                """.formatted(username, username, PASSWORD);
    }

    private String loginBody(String identifier, String password) {
        return """
                {"identifier": "%s", "password": "%s"}
                """.formatted(identifier, password);
    }

    private String createEventBody() {
        return """
                {"eventCode": "%s", "name": "Caribbean Music Fest 2026", "description": "Festival",
                 "category": "MUSIC", "eventDate": "%s", "minimumAge": 18, "venueCode": "VEN-SMR-01"}
                """.formatted(EVENT_CODE, LocalDateTime.now().plusDays(30).withNano(0));
    }

    private String login(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(username, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    private String buyTicket(String token, String email) throws Exception {
        String body = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userEmail": "%s", "eventCode": "%s", "type": "GENERAL"}
                                """.formatted(email, EVENT_CODE)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.ticketCode");
    }
}