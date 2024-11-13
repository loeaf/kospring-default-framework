package com.service.frame.siginin.service;

import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.model.User;

import java.lang.reflect.InvocationTargetException;

public interface SigininService {
    User save(UserParam user) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
    String signUp(UserParam user) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
}
