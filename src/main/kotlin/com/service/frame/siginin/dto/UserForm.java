package com.service.frame.siginin.dto;

import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.Length;

@Getter
@Setter
public class UserForm {
    private String email;
    private String password;
    private String nickName;
}
