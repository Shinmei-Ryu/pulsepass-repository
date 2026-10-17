package com.pulse.pass.service.impl;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserProfile;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
import com.pulse.pass.exception.BusinessRuleException;
import com.pulse.pass.exception.DuplicateResourceException;
import com.pulse.pass.exception.ResourceNotFoundException;
import com.pulse.pass.mapper.UserMapper;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserProfileRepository;
import com.pulse.pass.repository.UserRepository;
import com.pulse.pass.repository.UserRoleRepository;
import com.pulse.pass.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           UserCredentialRepository userCredentialRepository,
                           UserRoleRepository userRoleRepository,
                           PasswordEncoder passwordEncoder,
                           UserMapper mapper) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mapper = mapper;
    }

    // FR-SVC-010 / BR-USER-001..005 / FR-SEC (credencial y rol inicial)
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

        // BCrypt trunca/rechaza por bytes, no por caracteres
        if (request.password().getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
            throw new BusinessRuleException("Password cannot exceed " + BCRYPT_MAX_PASSWORD_BYTES + " bytes.");
        }

        // BR-USER-003: active = true
        User user = new User(request.username(), request.email(), true);
        User savedUser = userRepository.save(user);

        // BR-USER-004: User, UserProfile, credencial y rol se crean en la misma transacción
        UserProfile profile = new UserProfile(
                request.firstName(), request.lastName(), request.phone(),
                request.city(), request.birthDate()
        );
        profile.setUser(savedUser);
        userProfileRepository.save(profile);
        savedUser.setUserProfile(profile);

        userCredentialRepository.save(new UserCredential(savedUser, passwordEncoder.encode(request.password())));

        // El rol inicial siempre es USER: nunca viene del request
        userRoleRepository.save(new UserRole(savedUser, Role.USER));

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