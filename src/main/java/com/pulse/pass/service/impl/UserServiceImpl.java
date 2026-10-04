package com.pulse.pass.service.impl;

import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserProfile;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.DuplicateResourceException;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.UserMapper;
import com.pulse.pass.repository.UserProfileRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.service.UserService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository userRepository, UserProfileRepository userProfileRepository,
                           UserMapper mapper) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.mapper = mapper;
    }

    // FR-SVC-010 / BR-USER-001..005
    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {

        // BR-USER-001
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        // BR-USER-002
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        // BR-USER-005
        if (request.birthDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Birth date cannot be in the future.");
        }

        // BR-USER-003: active = true por defecto (constructor de User)
        User user = new User(request.username(), request.email(), true);
        User savedUser = userRepository.save(user);

        // BR-USER-004: User y UserProfile se crean en la misma transacción
        UserProfile profile = new UserProfile(
                request.firstName(), request.lastName(), request.phone(),
                request.city(), request.birthDate()
        );
        profile.setUser(savedUser);
        userProfileRepository.save(profile);

        savedUser.setUserProfile(profile);

        return mapper.toResponse(savedUser);
    }

    // FR-SVC-011
    @Override
    public UserResponse findByEmail(String email) {

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        return mapper.toResponse(user);
    }

    // FR-SVC-012
    @Override
    public UserResponse findByUsername(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        return mapper.toResponse(user);
    }
}