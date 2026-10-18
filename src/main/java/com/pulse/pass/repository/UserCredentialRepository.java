package com.pulse.pass.repository;

import com.pulse.pass.domain.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    // Login por usuario (exacto, como en el registro) o por correo (sin distinguir mayúsculas)
    @Query("""
            select c
            from UserCredential c
            join fetch c.user u
            where u.username = :identifier
               or lower(u.email) = lower(:identifier)
            """)
    List<UserCredential> findByUserIdentifier(@Param("identifier") String identifier);

    // Para recargar el principal desde el token: credencial + usuario en una sola consulta
    @Query("""
            select c
            from UserCredential c
            join fetch c.user
            where c.userId = :userId
            """)
    Optional<UserCredential> findWithUserByUserId(@Param("userId") Long userId);
}