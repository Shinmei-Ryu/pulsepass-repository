package com.pulse.pass.controller;

import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
import com.pulse.pass.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // FR-CTRL-USR-001
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = service.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // FR-CTRL-USR-002
    @GetMapping("/by-email")
    public ResponseEntity<UserResponse> findByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.findByEmail(email));
    }

    // FR-CTRL-USR-003
    @GetMapping("/by-username")
    public ResponseEntity<UserResponse> findByUsername(@RequestParam String username) {
        return ResponseEntity.ok(service.findByUsername(username));
    }
}