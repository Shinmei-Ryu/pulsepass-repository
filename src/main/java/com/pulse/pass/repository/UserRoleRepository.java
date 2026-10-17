package com.pulse.pass.repository;

import com.pulse.pass.domain.UserRole;
import com.pulse.pass.domain.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByUser_Id(Long userId);
}