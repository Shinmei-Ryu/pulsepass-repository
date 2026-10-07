package com.pulse.pass.dto.request;

import com.pulse.pass.domain.EventCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank(message = "Event code is required")
        String eventCode,
        @NotBlank(message = "Name is required")
        String name,
        @Size(max = 500, message = "Description cannot contain more than 500 characters")
        String description,
        @NotNull(message = "Category is required")
        EventCategory category,
        @NotNull(message = "Event date is required")
        LocalDateTime eventDate,
        @NotNull(message = "Minimum age is required")
        @Min(value = 0 , message = "Minimum age cannot be negative")
        Integer minimumAge,
        @NotBlank(message = "Venue code is required")
        String venueCode
) {}

