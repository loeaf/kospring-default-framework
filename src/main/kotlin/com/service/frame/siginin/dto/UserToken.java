package com.service.frame.siginin.dto;

import com.service.frame.siginin.model.User;
import com.service.frame.siginin.service.UserService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class UserToken {
    private User user;
    private String token;

    @Autowired
    private UserService userService;

    public User findUserByDb() {
        return this.userService.findById(this.user.getId());
    }
}