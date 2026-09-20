package com.pulse.pass.repository;

import com.pulse.pass.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // FR-USR-001: recuperar usuario por username
    Optional<User> findByUsername(String username);

    // Sección 14: buscar usuario por email ignorando mayúsculas
    Optional<User> findByEmailIgnoreCase(String email);
}
