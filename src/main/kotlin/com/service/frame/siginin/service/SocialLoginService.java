package com.service.frame.siginin.service;

import com.service.frame.siginin.dto.SocialAuthResponse;
import com.service.frame.siginin.model.Account;
import org.springframework.stereotype.Service;

@Service
public interface SocialLoginService {
    SocialAuthResponse getAccessToken(String authorizationCode);
    Account getUserInfo(String accessToken);
}
