package com.service.frame.siginin.repository;

import com.service.frame.siginin.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
    User findByNickName(String nickName);
}
