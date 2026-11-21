package com.pulse.pass.dto.response;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String username,
        String email,
        Boolean active,
        String firstName,
        String lastName,
        String city,
        // Solo llega al propio usuario o a un ADMIN (ver @PreAuthorize en UserServiceImpl)
        LocalDate birthDate
) {
}
