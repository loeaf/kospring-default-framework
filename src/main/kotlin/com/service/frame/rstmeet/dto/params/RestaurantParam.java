package com.service.frame.rstmeet.dto.params;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantParam {
    private String id;
    private Double longitude;
    private Double latitude;
    // 맛집번호
    private Integer restaurantNumber;
    // 맛집명
    private String name;
    // 한글도로명주소
    private String roadAddress;
    // 한글지번주소
    private String jibunAddress;
    // 영어주소
    private String englishAddress;
    // 요약주소
    private String miniAddress;
    // 위도
    // 등록일
    private Date regDate;
    // 전화번호
    private String phoneNumber;
    // 휴무일
    private String holiday;
    // 인스타 아이디 참조값
    private String instaIdRef;
    // 참고URL
    private String referenceUrl;
    // 데이터종류
    private String dataType;
    // 대표메뉴
    private String representativeMenu;
    private Integer refinedGeoLocation;
    // 리뷰정보
    private String reViewContent;
    // 인스타그램 아이디
    private String instagramId;
    // 숏코드
    private String shortCode;
    private String selectedSumnail;
    private String location;
    private MediaParam media;
}