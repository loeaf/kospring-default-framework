package com.service.frame.util;

import com.service.frame.constatns.GlobalConstants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class SesUtil {
	private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	
	public void setSesNone(HttpServletRequest request, HttpServletResponse response) {
		try {
			CookieUtil cookieUtil = new CookieUtil(request);
			String sesCId = cookieUtil.getValue(GlobalConstants.SESSION_COOKIE_NAME);
			response.setHeader(HttpHeaders.SET_COOKIE, GlobalConstants.SESSION_COOKIE_NAME + "=" + sesCId + "; path=/; HttpOnly; SameSite=None");
		} catch (IOException e) {
			logger.debug("setSesNone IOException");
		}
		
	}
}
