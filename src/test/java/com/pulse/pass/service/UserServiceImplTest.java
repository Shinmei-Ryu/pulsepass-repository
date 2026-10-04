package com.pulse.pass.service;

import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserProfile;
import com.pulse.pass.dto.request.RegisterUserRequest;
import com.pulse.pass.dto.response.UserResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    private static final String USERNAME = "andrea";
    private static final String EMAIL = "andrea@email.com";

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
    @DisplayName("TEST-USER-001 / BR-USER-003 / BR-USER-004: valid registration creates active User and UserProfile")
    void register_validRequest_createsActiveUserAndProfile() {
        // ARRANGE
        RegisterUserRequest request = registerRequest(LocalDate.of(2000, 5, 10));
        UserResponse expected = userResponse();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(EMAIL)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toResponse(any(User.class))).thenReturn(expected);

        // ACT
        UserResponse result = service.register(request);

        // ASSERT
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        InOrder inOrder = inOrder(userRepository, userProfileRepository);
        inOrder.verify(userRepository).save(userCaptor.capture());
        inOrder.verify(userProfileRepository).save(profileCaptor.capture());

        User savedUser = userCaptor.getValue();
        UserProfile savedProfile = profileCaptor.getValue();

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

        assertThat(result).isEqualTo(expected);
        verify(mapper).toResponse(savedUser);
    }


    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private RegisterUserRequest registerRequest(LocalDate birthDate) {
        return new RegisterUserRequest(USERNAME, EMAIL, "Andrea", "Lopez",
                "3001234567", "Santa Marta", birthDate);
    }

    private UserResponse userResponse() {
        return new UserResponse(1L, USERNAME, EMAIL, true, "Andrea", "Lopez", "Santa Marta");
    }
}
