package com.service.frame.rstmeet.dto;

import lombok.Data;

@Data
public class GoogleJsonResponse {
    // 구글 데이터를 저장할때 만드는 uuid
    String uuid;
    // 텍스트 json
    String jsonText;
    // 구글 api url
    String googleApiUrl;
}