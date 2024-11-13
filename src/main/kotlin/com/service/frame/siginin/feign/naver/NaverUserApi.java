package com.service.frame.siginin.feign.naver;

import com.service.frame.siginin.config.FeignConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(value = "naverUser", url="https://openapi.naver.com", configuration = {FeignConfiguration.class})
public interface NaverUserApi {
    @GetMapping("/v1/nid/me")
    ResponseEntity<String> getUserInfo(@RequestHeader Map<String, String> header);
}
