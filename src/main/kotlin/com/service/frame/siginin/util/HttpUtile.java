package com.service.frame.siginin.util;

import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpUtile {
    public static String path = "";
    public static RestTemplate restTemplate = new RestTemplate();

    public void setKey(String value) {
        this.path = value;
    }
    public static String buildQueryParams(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(key);
            sb.append("=");
            sb.append(value);
        }
        return sb.toString();
    }
    public static <T> T getJsonReponse(String url, HashMap<String, String> param, Class<T> type) {
        if (param != null) {
            try {
                url += "?" + buildQueryParams(param);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        var result = restTemplate.getForObject(url, type);
        return result;
    }

    public static <T> T postJsonResponse(String url, Map<String, Object> param, Class<T> type) {
        return (T) restTemplate.postForObject(url, param, type);
    }

}