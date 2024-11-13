package com.service.frame.datacreator.model;

import com.service.frame.common.domain.Domain;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant extends Domain {

    private String dataType;

    private String englishAddress;

    private String holiday;

    private String jibunAddress;

    private Double latitude;

    private Double longitude;

    private String miniAddress;

    private String name;

    private String phoneNumber;

    private String referenceUrl;

    private Integer refinedGeoLocation;

    private Date regDate;

    private String representativeMenu;

    private Integer restaurantNumber;

    private String roadAddress;

    private Date updateDate;

    private String cityTypeId;

    private String countryTypeId;

    private String foodTypeId;

    private String instaInfoId;

    private String geoInfo;

    private String googleCallApiUrl;

    private String googleCallDataUuid;
}