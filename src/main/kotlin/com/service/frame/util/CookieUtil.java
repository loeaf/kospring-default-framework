package com.service.frame.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CookieUtil {
    private final Map<String, Cookie> cookieMap = new ConcurrentHashMap<String, Cookie>();
    
    public CookieUtil(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie);
            }
        }
    }
   
    public static Cookie createCookie(String name, String value)
    throws IOException {
        return new Cookie(name, URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
    
    public static Cookie deleteCookie(String name)
    throws IOException {
    	Cookie cookie = new Cookie(name, null);
    	cookie.setMaxAge(0);
        return cookie;
    }

    public static Cookie createCookie(
            String name, String value, String path, int maxAge) 
    throws IOException {
        Cookie cookie = new Cookie(name, 
                                URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
    
    public static Cookie createCookie(
            String name, String value,  
            String domain, String path, int maxAge) 
    throws IOException {
        Cookie cookie = new Cookie(name, 
                  URLEncoder.encode(value, StandardCharsets.UTF_8));
        cookie.setDomain(domain);
        cookie.setPath(path);
        cookie.setMaxAge(maxAge);
        return cookie;
    }
    
    public Cookie getCookie(String name) {
        return (Cookie)cookieMap.get(name); 
    }
    
    public String getValue(String name) throws IOException {
        Cookie cookie = (Cookie)cookieMap.get(name);
        if (cookie == null) {
        	return null;
        }
        return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
    }
    
    public boolean exists(String name) {
        return cookieMap.get(name) != null;
    }
}
