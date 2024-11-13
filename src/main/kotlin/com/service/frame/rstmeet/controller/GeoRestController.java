package com.service.frame.rstmeet.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.dto.VworldAddressResult;
import com.service.frame.rstmeet.dto.VworldWrapResponse;
import com.service.frame.rstmeet.dto.params.LocationParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/geo")
public class GeoRestController {
    @Value("${key.vworld}")
    String vworldKey;
    //
    @PostMapping("/location")
    @ApiOperation(value = "위치정보 획득")
    public ResponseEntity<Object> registNation(@RequestBody LocationParam locationParam) throws Exception {
        // get reuqest http 36.473605118249,127.2886575174
        String lon = locationParam.getLongitude();
        String lat = locationParam.getLatitude();
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder stringBuilder = new StringBuilder();
        VworldAddressResult vworldAddressResult = new  VworldAddressResult();
        stringBuilder
                .append("http://api.vworld.kr/req/address?service=address&request=getAddress&version=2.0&crs=epsg:4326&")
                .append("point=")
                .append(lon)
                .append(",")
                .append(lat)
                .append("&format=json&type=both&zipcode=true&simple=false&")
                .append("key=")
                .append(vworldKey);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(stringBuilder.toString(), String.class);
        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            VworldWrapResponse myResponseObject = objectMapper.readValue(responseBody, VworldWrapResponse.class);
            vworldAddressResult = myResponseObject.getResponse().getResult().get(0);
        } catch (JsonProcessingException e) {
            new Exception("JsonProcessingException");
//            e.printStackTrace();
        }

        return ResponseEntity.ok(new ResResult(vworldAddressResult));
    }
}
