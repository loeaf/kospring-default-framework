package com.service.frame.siginin.util;

import com.service.frame.siginin.model.User;
import com.service.frame.siginin.security.CustomUserInfo;
import com.service.frame.siginin.service.UserService;
import javax.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Data
@Component
public class UserInfoUtil {
    @Autowired
    private UserService beanUser;
    static UserService userService;

    @PostConstruct
    private void initialize() {
        userService = beanUser;
    }

    /**
     * 현 유저 권한에 대한 정보를 가져온다.
     * @return
     */
    public static CustomUserInfo get() {
        CustomUserInfo userDetails = (CustomUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails;
    }
    /**
     * 현 유저 권한에 대한 정보를 가져온다.
     * @return
     */
    public static User getMyUserObj() {
        CustomUserInfo userDetails = (CustomUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var p = userService.findById(userDetails.getId());
        return p;
    }

}
