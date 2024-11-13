package com.service.frame.config;

import com.service.frame.common.misc.DateUtils;
import com.service.frame.siginin.dto.UserToken;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.util.JwtManager;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;


@Component
public class GlobalHttpInterceptor implements HandlerInterceptor {
    @Autowired
    JwtManager jwtManager;

    @Autowired
    UserToken accountToken;

    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(DateUtils.class);
    String[] excludeUrl = {
            "/google",
            "/login",
            "/signUp",
            "/logout",
            "/Restaurant",
            "/location",
            "/Restaurant/findRestByInstaId",
            "/File/upload",
            "/resources"
    };
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        for (int i = 0; i < excludeUrl.length; i++) {
            if (request.getRequestURI().contains(excludeUrl[i])) {
                return true;
            }
        }

        //로그인 경로 제외
//        if(request.getRequestURI().contains("/login") ||
//                request.getRequestURI().contains("/signUp") ||
//                request.getRequestURI().contains("/logout") ||
//                request.getRequestURI().contains("/Restaurant") ||
//                request.getRequestURI().contains("/location") ||
//                request.getRequestURI().contains("/location") ||
//                request.getRequestURI().contains("/Restaurant/findRestByInstaId") ||
//                request.getRequestURI().contains("/File/upload") ||
//                request.getRequestURI().contains("/resources")){
//            return true;
//        }

        String authHeader = request.getHeader("Authorization");
        // Check if the Authorization header is present and starts with "Bearer"
        if (authHeader != null && authHeader.startsWith("Bearer")) {
            // Extract the JWT token from the Authorization header
            String jwtToken = authHeader.substring(7);
            if(jwtToken.equals("null")){
                response.sendError(401, "Session Expired");
                return false;
            }
            // Use the JWT token for authentication or other purposes
            // For example, you can decode the token using a library like jjwt
            User account = null;
            try {
                account = this.jwtManager.getAccountByToken(jwtToken);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Unable to get JWT Token", e);
                response.sendError(401, "Session Unable");
            }  catch (ExpiredJwtException e) {
                LOGGER.error("the token is expired and not valid anymore", e);
                response.sendError(401, "Session Expired");
                return false;
            } catch (Exception e) {
                LOGGER.error("the token is invalid", e);
                response.sendError(401, "Invalid Token");
                return false;
            }
            accountToken.setUser(account);
            accountToken.setToken(jwtToken);
        }

        // Continue processing the request
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        var jwtToken = jwtManager.generateJwtToken(this.accountToken.getAccount());
//        response.addHeader("Authorization", "Basic " + jwtToken);
        System.out.println("postHandle");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("afterCompletion");
    }

}