package com.service.frame.siginin.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.siginin.model.Role;
import com.service.frame.siginin.repository.RoleRepository;
import com.service.frame.siginin.service.RoleService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl
        extends ServiceImpl<RoleRepository, Role, Long>
        implements RoleService {
    private final RoleRepository roleRepository;

    @PostConstruct
    private void init() {
        super.set(roleRepository, new Role());
    }

}
