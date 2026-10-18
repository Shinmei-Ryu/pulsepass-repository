package com.pulse.pass.security;

import com.pulse.pass.domain.Role;
import com.pulse.pass.domain.User;
import com.pulse.pass.domain.UserCredential;
import com.pulse.pass.domain.UserRole;
import com.pulse.pass.repository.UserCredentialRepository;
import com.pulse.pass.repository.UserRoleRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserCredentialRepository credentialRepository;
    private final UserRoleRepository roleRepository;

    public CustomUserDetailsService(UserCredentialRepository credentialRepository,
                                    UserRoleRepository roleRepository) {
        this.credentialRepository = credentialRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        List<UserCredential> matches = credentialRepository.findByUserIdentifier(identifier);

        // Ninguno (inexistente o sin credencial) o ambiguo: mismo resultado, sin revelar nada
        if (matches.size() != 1) {
            throw new UsernameNotFoundException("User not found");
        }

        UserCredential credential = matches.get(0);
        User user = credential.getUser();

        return new SecurityPrincipal(
                user.getId(), user.getUsername(), user.getEmail(),
                credential.getPasswordHash(), user.getActive(), rolesOf(user)
        );
    }

    // Para el filtro JWT: identidad, estado y roles frescos desde la BD en cada request
    public SecurityPrincipal loadPrincipalById(Long userId) {

        UserCredential credential = credentialRepository.findWithUserByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        User user = credential.getUser();

        // Sin hash: este principal sirve para autorizar, no para autenticar con contraseña
        return new SecurityPrincipal(
                user.getId(), user.getUsername(), user.getEmail(),
                null, user.getActive(), rolesOf(user)
        );
    }

    private Set<Role> rolesOf(User user) {
        return roleRepository.findByUser_Id(user.getId())
                .stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
    }
}