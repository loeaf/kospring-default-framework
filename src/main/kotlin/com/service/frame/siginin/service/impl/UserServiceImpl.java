package com.service.frame.siginin.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.repository.UserRepository;
import com.service.frame.siginin.service.UserService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<UserRepository, User, String>
        implements UserService {
    private final UserRepository userRepository;

    @PostConstruct
    private void init() {
        super.set(userRepository, new User());
    }
    @Override
    public User findByNickName(String nickName) {
        return userRepository.findByNickName(nickName);
    }
}
