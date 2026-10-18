package com.pulse.pass.service;

import com.pulse.pass.dto.request.LoginRequest;
import com.pulse.pass.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse login(LoginRequest request);
}