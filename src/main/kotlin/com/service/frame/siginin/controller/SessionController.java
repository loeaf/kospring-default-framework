package com.service.frame.siginin.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/session")
public class SessionController {
    @GetMapping("session")
    public ResponseEntity<Object> checkJwt(HttpServletRequest request,
                                           @RequestParam String jwt) {
        return null;
    }

    /**
     * 로그인 세션 정보 가져오기
     *
     * @param request
     * @return
     */
//    @PostMapping(value = "/api/userSession")
//    public ResponseEntity<SessionDTO> getUserSession(HttpServletRequest request) {
//
//        // 세션 가져오기
//        ObjectMapper mapper = new ObjectMapper();
//        SessionDTO sessionDto = mapper.convertValue(request.getSession().getAttribute(SESSION_NAME), SessionDTO.class);
//        LOGGER.debug("사용자ID = " + sessionDto.getSession_usrid());
//        LOGGER.debug("사용자이름 = " + sessionDto.getSession_usrname());
//        LOGGER.debug("기관ID = " + sessionDto.getSession_instcd());
//        LOGGER.debug("기관이름 = " + sessionDto.getSession_instname());
//        LOGGER.debug("조직ID = " + sessionDto.getSession_orgid());
//        LOGGER.debug("조직이름 = " + sessionDto.getSession_orgname());
//
//        return new ResponseEntity<SessionDTO>(sessionDto, HttpStatus.OK);
//    }

}
