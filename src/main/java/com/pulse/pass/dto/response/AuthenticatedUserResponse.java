package com.pulse.pass.dto.response;

import java.util.List;

public record AuthenticatedUserResponse(
        Long id,
        String username,
        String email,
        List<String> roles
) {
}