package com.pulse.pass.repository;

import com.pulse.pass.domain.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    // FR-USR-003 / FR-USR-004: perfil de un usuario navegando la relación user.id
    Optional<UserProfile> findByUser_Id(Long userId);
}
