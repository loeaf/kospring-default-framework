package com.service.frame.siginin.service.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.service.frame.siginin.dto.GoogleLoginResponse;
import com.service.frame.siginin.dto.GoogleRequestAccessTokenDto;
import com.service.frame.siginin.dto.SocialAuthResponse;
import com.service.frame.siginin.feign.google.GoogleAuthApi;
import com.service.frame.siginin.feign.google.GoogleUserApi;
import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.service.SocialLoginService;
import com.service.frame.siginin.util.GsonLocalDateTimeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class GoogleLoginServiceImpl implements SocialLoginService {
    Logger log = LoggerFactory.getLogger(this.getClass());
    private final GoogleAuthApi googleAuthApi;
    private final GoogleUserApi googleUserApi;

    @Value("${social.client.google.rest-api-key}")
    private String googleAppKey;
    @Value("${social.client.google.secret-key}")
    private String googleAppSecret;
    @Value("${social.client.google.redirect-uri}")
    private String googleRedirectUri;
    @Value("${social.client.google.grant_type}")
    private String googleGrantType;

    public GoogleLoginServiceImpl(GoogleAuthApi googleAuthApi, GoogleUserApi googleUserApi) {
        this.googleAuthApi = googleAuthApi;
        this.googleUserApi = googleUserApi;
    }


    @Override
    public SocialAuthResponse getAccessToken(String authorizationCode) {
        ResponseEntity<?> response = googleAuthApi.getAccessToken(
                GoogleRequestAccessTokenDto.builder()
                        .code(authorizationCode)
                        .client_id(googleAppKey)
                        .clientSecret(googleAppSecret)
                        .redirect_uri(googleRedirectUri)
                        .grant_type(googleGrantType)
                        .build()
        );

        log.info("google auth info");
        log.info(response.toString());

        return new Gson()
                .fromJson(
                        response.getBody().toString(),
                        SocialAuthResponse.class
                );
    }

    @Override
    public Account getUserInfo(String accessToken) {
        ResponseEntity<?> response = googleUserApi.getUserInfo(accessToken);

        log.info("google user response");
        log.info(response.toString());

        String jsonString = response.getBody().toString();

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter())
                .create();

        GoogleLoginResponse googleLoginResponse = gson.fromJson(jsonString, GoogleLoginResponse.class);
        return Account.builder()
//                .email(googleLoginResponse.getEmail())
//                .name(googleLoginResponse.getName())
//                .picture(googleLoginResponse.getPicture())
                .build();
    }
}