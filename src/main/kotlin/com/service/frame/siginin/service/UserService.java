package com.service.frame.siginin.service;

import com.service.frame.common.misc.Service;
import com.service.frame.siginin.model.User;

public interface UserService extends Service<User, String> {
    public User findByNickName(String nickName);
}
