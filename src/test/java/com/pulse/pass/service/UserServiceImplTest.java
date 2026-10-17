package com.pulse.pass.service;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.pulse.pass.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final String USERNAME = "andrea";
    private static final String EMAIL = "andrea@email.com";
    private static final String PASSWORD = "a-strong-password";

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserMapper mapper;

    @InjectMocks
    private UserServiceImpl service;

    // ------------------------------------------------------------------
    // register
    // ------------------------------------------------------------------

    @Test
    @DisplayName("TEST-USER-001 / BR-USER-003 / BR-USER-004: valid registration creates active User, UserProfile, hashed credential and USER role")
    void register_validRequest_createsUserProfileCredentialAndRole() {
        // ARRANGE
        RegisterUserRequest request = registerRequest(LocalDate.of(2000, 5, 10));
        UserResponse expected = userResponse();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded-hash");
        when(mapper.toResponse(any(User.class))).thenReturn(expected);

        // ACT
        UserResponse result = service.register(request);

        // ASSERT
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        ArgumentCaptor<UserCredential> credentialCaptor = ArgumentCaptor.forClass(UserCredential.class);
        ArgumentCaptor<UserRole> roleCaptor = ArgumentCaptor.forClass(UserRole.class);
        InOrder inOrder = inOrder(userRepository, userProfileRepository, userCredentialRepository, userRoleRepository);
        inOrder.verify(userRepository).save(userCaptor.capture());
        inOrder.verify(userProfileRepository).save(profileCaptor.capture());
        inOrder.verify(userCredentialRepository).save(credentialCaptor.capture());
        inOrder.verify(userRoleRepository).save(roleCaptor.capture());

        User savedUser = userCaptor.getValue();
        UserProfile savedProfile = profileCaptor.getValue();
        UserCredential savedCredential = credentialCaptor.getValue();
        UserRole savedRole = roleCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo(USERNAME);
        assertThat(savedUser.getEmail()).isEqualTo(EMAIL);
        assertThat(savedUser.getActive()).isTrue();
        assertThat(savedUser.getUserProfile()).isSameAs(savedProfile);

        assertThat(savedProfile.getUser()).isSameAs(savedUser);
        assertThat(savedProfile.getFirstName()).isEqualTo("Andrea");
        assertThat(savedProfile.getLastName()).isEqualTo("Lopez");
        assertThat(savedProfile.getPhone()).isEqualTo("3001234567");
        assertThat(savedProfile.getCity()).isEqualTo("Santa Marta");
        assertThat(savedProfile.getBirthDate()).isEqualTo(LocalDate.of(2000, 5, 10));

        assertThat(savedCredential.getUser()).isSameAs(savedUser);
        assertThat(savedCredential.getPasswordHash()).isEqualTo("encoded-hash").isNotEqualTo(PASSWORD);

        assertThat(savedRole.getUser()).isSameAs(savedUser);
        assertThat(savedRole.getRole()).isEqualTo(Role.USER);

        assertThat(result).isEqualTo(expected);
        verify(mapper).toResponse(savedUser);
    }

    @Test
    @DisplayName("TEST-USER-002 / BR-USER-001: duplicated username throws DuplicateResourceException")
    void register_duplicatedUsername_throwsDuplicateResource() {
        // ARRANGE
        RegisterUserRequest request = registerRequest(LocalDate.of(2000, 5, 10));
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        // ACT
        Throwable thrown = catchThrowable(() -> service.register(request));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(USERNAME);
        verify(userRepository, never()).existsByEmailIgnoreCase(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("TEST-USER-003 / BR-USER-002: duplicated email throws DuplicateResourceException")
    void register_duplicatedEmail_throwsDuplicateResource() {
        // ARRANGE
        RegisterUserRequest request = registerRequest(LocalDate.of(2000, 5, 10));
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(true);

        // ACT
        Throwable thrown = catchThrowable(() -> service.register(request));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(EMAIL);
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("TEST-USER-004 / BR-USER-005: future birth date throws BusinessRuleException")
    void register_futureBirthDate_throwsBusinessRule() {
        // ARRANGE
        RegisterUserRequest request = registerRequest(LocalDate.now().plusDays(1));
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);

        // ACT
        Throwable thrown = catchThrowable(() -> service.register(request));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
        verify(userProfileRepository, never()).save(any(UserProfile.class));
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
        verify(userRoleRepository, never()).save(any(UserRole.class));
        verifyNoInteractions(passwordEncoder);
    }

    // ------------------------------------------------------------------
    // findByEmail
    // ------------------------------------------------------------------

    @Test
    void findByEmail_existingUser_returnsDto() {
        // ARRANGE
        User user = new User(USERNAME, EMAIL, true);
        UserResponse expected = userResponse();
        when(userRepository.findByEmailIgnoreCase(eq(EMAIL))).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(expected);

        // ACT
        UserResponse result = service.findByEmail(EMAIL);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(userRepository).findByEmailIgnoreCase(EMAIL);
    }

    @Test
    void findByEmail_missingUser_throwsResourceNotFound() {
        // ARRANGE
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.findByEmail(EMAIL));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(EMAIL);
        verify(mapper, never()).toResponse(any(User.class));
    }

    // ------------------------------------------------------------------
    // findByUsername
    // ------------------------------------------------------------------

    @Test
    void findByUsername_existingUser_returnsDto() {
        // ARRANGE
        User user = new User(USERNAME, EMAIL, true);
        UserResponse expected = userResponse();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(expected);

        // ACT
        UserResponse result = service.findByUsername(USERNAME);

        // ASSERT
        assertThat(result).isEqualTo(expected);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void findByUsername_missingUser_throwsResourceNotFound() {
        // ARRANGE
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // ACT
        Throwable thrown = catchThrowable(() -> service.findByUsername(USERNAME));

        // ASSERT
        assertThat(thrown)
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(USERNAME);
        verify(mapper, never()).toResponse(any(User.class));
    }

    @Test
    @DisplayName("SEC: password longer than 72 bytes throws BusinessRuleException")
    void register_passwordLongerThan72Bytes_throwsBusinessRule() {
        // ARRANGE
        String multibytePassword = "€".repeat(25); // 25 caracteres, 75 bytes en UTF-8
        RegisterUserRequest request = new RegisterUserRequest(USERNAME, EMAIL, multibytePassword,
                "Andrea", "Lopez", "3001234567", "Santa Marta", LocalDate.of(2000, 5, 10));
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);

        // ACT
        Throwable thrown = catchThrowable(() -> service.register(request));

        // ASSERT
        assertThat(thrown).isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }


    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private RegisterUserRequest registerRequest(LocalDate birthDate) {
        return new RegisterUserRequest(USERNAME, EMAIL, PASSWORD, "Andrea", "Lopez",
                "3001234567", "Santa Marta", birthDate);
    }

    private UserResponse userResponse() {
        return new UserResponse(1L, USERNAME, EMAIL, true, "Andrea", "Lopez", "Santa Marta");
    }
}
