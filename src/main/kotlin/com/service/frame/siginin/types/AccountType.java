package com.service.frame.siginin.types;

public enum AccountType {
    EMAIL("EMAIL"),
    FACEBOOK("FACEBOOK"),
    TWITTER("TWITTER"),
    GOOGLE("GOOGLE"),
    KAKAO("KAKAO"),
    NAVER("NAVER");

    private String value;

    AccountType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
