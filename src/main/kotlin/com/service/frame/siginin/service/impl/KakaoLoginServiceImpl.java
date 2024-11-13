package com.service.frame.siginin.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.service.frame.siginin.dto.KaKaoLoginResponse;
import com.service.frame.siginin.dto.SocialAuthResponse;
import com.service.frame.siginin.feign.kakao.KakaoAuthApi;
import com.service.frame.siginin.feign.kakao.KakaoUserApi;
import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.service.SocialLoginService;
import com.service.frame.siginin.util.GsonLocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class KakaoLoginServiceImpl implements SocialLoginService {

    Logger log = LoggerFactory.getLogger(this.getClass());
    private final KakaoAuthApi kakaoAuthApi;
    private final KakaoUserApi kakaoUserApi;

    @Value("${social.client.kakao.rest-api-key}")
    private String kakaoAppKey;
    @Value("${social.client.kakao.secret-key}")
    private String kakaoAppSecret;
    @Value("${social.client.kakao.redirect-uri}")
    private String kakaoRedirectUri;
    @Value("${social.client.kakao.grant_type}")
    private String kakaoGrantType;

    public KakaoLoginServiceImpl(KakaoAuthApi kakaoAuthApi, KakaoUserApi kakaoUserApi) {
        this.kakaoAuthApi = kakaoAuthApi;
        this.kakaoUserApi = kakaoUserApi;
    }


    @Override
    public SocialAuthResponse getAccessToken(String authorizationCode) {
        ResponseEntity<?> response = kakaoAuthApi.getAccessToken(
                kakaoAppKey,
                kakaoAppSecret,
                kakaoGrantType,
                kakaoRedirectUri,
                authorizationCode
        );

        log.info("kaka auth response {}", response.toString());

        return new Gson()
                .fromJson(
                        String.valueOf(response.getBody())
                        , SocialAuthResponse.class
                );
    }

    @Override
    public Account getUserInfo(String accessToken) {
        Map<String ,String> headerMap = new HashMap<>();
        headerMap.put("authorization", "Bearer " + accessToken);

        ResponseEntity<?> response = kakaoUserApi.getUserInfo(headerMap);

        log.info("kakao user response");
        log.info(response.toString());

        String jsonString = response.getBody().toString();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
                .create();

        KaKaoLoginResponse kaKaoLoginResponse = gson.fromJson(jsonString, KaKaoLoginResponse.class);
        KaKaoLoginResponse.KakaoLoginData kakaoLoginData = Optional.ofNullable(kaKaoLoginResponse.getKakao_account())
                .orElse(KaKaoLoginResponse.KakaoLoginData.builder().build());

        String name = Optional.ofNullable(kakaoLoginData.getProfile())
                .orElse(KaKaoLoginResponse.KakaoLoginData.KakaoProfile.builder().build())
                .getNickname();

        return Account.builder()
//                .id(kaKaoLoginResponse.getId())
//                .gender(kakaoLoginData.getGender())
//                .name(name)
//                .email(kakaoLoginData.getEmail())
                .build();
    }
}
