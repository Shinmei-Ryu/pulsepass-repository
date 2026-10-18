package com.pulse.pass.service;

import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;
import com.pulse.pass.dto.response.AuthenticatedUserResponse;
import com.pulse.pass.security.SecurityPrincipal;

public interface AuthService {

    AuthResponse login(LoginRequest request);
    AuthenticatedUserResponse currentUser(SecurityPrincipal principal);
}