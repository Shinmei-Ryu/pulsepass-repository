package com.pulse.pass.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterUserRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Email must have a valid format")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 12, max = 72, message = "Password must contain between 12 and 72 characters")
        String password,
        @NotBlank(message = "First name is required")
        String firstName,
        @NotBlank(message = "Last name is required")
        String lastName,
        String phone,
        String city,
        @NotNull(message = "Birth date is required")
        LocalDate birthDate
) {

        // El toString generado por el record imprimiría la contraseña
        @Override
        public String toString() {
                return "RegisterUserRequest[username=" + username
                        + ", email=" + email
                        + ", password=***"
                        + ", firstName=" + firstName
                        + ", lastName=" + lastName
                        + ", phone=" + phone
                        + ", city=" + city
                        + ", birthDate=" + birthDate + "]";
        }
}