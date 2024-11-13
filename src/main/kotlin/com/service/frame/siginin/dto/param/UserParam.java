package com.service.frame.siginin.dto.param;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserParam {
    private String id;
    /**
     * 인증 ID 또는 로그인 시 사용하는 ID
     */
    private String loginId;
    private String password;
    /**
     * EMAIL, FACEBOOK, TWITTER, GOOGLE, KAKAO
     */
    private String accountType;
    private String nickName;
}