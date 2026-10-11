package com.pulse.pass.dto.request;

import com.pulse.pass.domain.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PurchaseTicketRequest(
        @NotBlank(message = "User's email is required")
        @Email(message = "Email must have a valid format")
        String userEmail,
        @NotBlank(message = "Event code is required")
        String eventCode,
        @NotNull(message = "Ticket type is required")
        TicketType type
) {
}