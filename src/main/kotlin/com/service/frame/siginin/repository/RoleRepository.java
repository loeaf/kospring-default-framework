package com.service.frame.siginin.repository;

import com.service.frame.siginin.model.Role;
import com.service.frame.siginin.types.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleByAuthority(Authority authority);
}
