package com.pulse.pass.controller;

import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.DuplicateResourceException;
import com.pulse.pass.exception.GlobalExceptionHandler;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    private static final String VALID_BODY = """
            {
              "username": "andrea",
              "email": "andrea@email.com",
              "firstName": "Andrea",
              "lastName": "Lopez",
              "phone": "3001234567",
              "city": "Santa Marta",
              "birthDate": "2000-05-10"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService service;

    // ------------------------------------------------------------------
    // POST /api/users
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-CTRL-USR-001: valid register returns 201")
    void shouldRegisterUser() throws Exception {
        // ARRANGE
        when(service.register(any(RegisterUserRequest.class))).thenReturn(userResponse());

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.firstName").value("Andrea"))
                .andExpect(jsonPath("$.lastName").value("Lopez"))
                .andExpect(jsonPath("$.city").value("Santa Marta"));

        ArgumentCaptor<RegisterUserRequest> captor = ArgumentCaptor.forClass(RegisterUserRequest.class);
        verify(service).register(captor.capture());
        RegisterUserRequest sent = captor.getValue();
        assertThat(sent.username()).isEqualTo("andrea");
        assertThat(sent.email()).isEqualTo("andrea@email.com");
        assertThat(sent.firstName()).isEqualTo("Andrea");
        assertThat(sent.lastName()).isEqualTo("Lopez");
        assertThat(sent.birthDate()).isEqualTo(LocalDate.of(2000, 5, 10));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-002: invalid email returns 400 with details and never calls Service")
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("andrea@email.com", "not-an-email")))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.email").value("Email must be a valid email address"));

        verify(service, never()).register(any(RegisterUserRequest.class));
    }

    @Test
    void shouldReturn400WhenRequiredFieldsAreMissing() throws Exception {
        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "",
                                  "firstName": "",
                                  "lastName": "",
                                  "birthDate": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.details.username").value("Username is required"))
                .andExpect(jsonPath("$.details.email").value("Email is required"))
                .andExpect(jsonPath("$.details.firstName").value("First name is required"))
                .andExpect(jsonPath("$.details.lastName").value("Last name is required"))
                .andExpect(jsonPath("$.details.birthDate").value("Birth date is required"));

        verify(service, never()).register(any(RegisterUserRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-003 / BR-USER-001: duplicated username returns 409")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        // ARRANGE
        when(service.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Username already exists: andrea"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Username already exists: andrea"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).register(any(RegisterUserRequest.class));
    }

    @Test
    @DisplayName("AC-CTRL-008 / BR-USER-002: duplicated email returns 409")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        // ARRANGE
        when(service.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException("Email already exists: andrea@email.com"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: andrea@email.com"));

        verify(service).register(any(RegisterUserRequest.class));
    }

    @Test
    @DisplayName("BR-USER-005: future birth date rejected by Service returns 409")
    void shouldReturn409WhenBirthDateIsInTheFuture() throws Exception {
        // ARRANGE
        when(service.register(any(RegisterUserRequest.class)))
                .thenThrow(new BusinessRuleException("Birth date cannot be in the future."));

        // ACT + ASSERT
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY.replace("2000-05-10", "2999-01-01")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Birth date cannot be in the future."));

        verify(service).register(any(RegisterUserRequest.class));
    }

    @Test
    @DisplayName("TEST-CTRL-USR-004: find by email returns 200")
    void shouldReturnUserByEmail() throws Exception {
        // ARRANGE
        when(service.findByEmail("andrea@email.com")).thenReturn(userResponse());

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email").param("email", "andrea@email.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("andrea@email.com"))
                .andExpect(jsonPath("$.username").value("andrea"));

        verify(service).findByEmail("andrea@email.com");
        verify(service, never()).findByUsername(anyString());
    }

    @Test
    void shouldReturn404WhenUserEmailDoesNotExist() throws Exception {
        // ARRANGE
        when(service.findByEmail("nobody@email.com"))
                .thenThrow(new ResourceNotFoundException("User not found: nobody@email.com"));

        // ACT + ASSERT
        mockMvc.perform(get("/api/users/by-email").param("email", "nobody@email.com"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found: nobody@email.com"))
                .andExpect(jsonPath("$.details").isMap());

        verify(service).findByEmail("nobody@email.com");
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private UserResponse userResponse() {
        return new UserResponse(1L, "andrea", "andrea@email.com", true, "Andrea", "Lopez", "Santa Marta");
    }
}
