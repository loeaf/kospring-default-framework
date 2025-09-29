package com.service.frame.siginin.controller;

import com.service.frame.constatns.GlobalConstants;
import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.service.AccountService;
import com.service.frame.siginin.types.AccountType;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/login")
public class LoginController {
    public static final String SESSION_NAME = GlobalConstants.SESSION_NAME;
    public static final int SESSION_TIME_SEC = 1500;
    public static final int MAX_LOGIN_FAIL_CNT = 5;
    @Autowired
    private AccountService accountService;

    /**
     *
     * @param request
     * @param userForm loginId, password, accountType을 입력 받음.
     *                 accountType은 EMAIL, FACEBOOK, TWITTER, GOOGLE, KAKAO 중 하나.
     * @return
     */
    @PostMapping("")
    public ResponseEntity<ResResult> login(HttpServletRequest request,
                                           @RequestBody UserParam userForm) {
        String jwt = accountService.login(userForm);
        return ResponseEntity.ok(new ResResult(jwt));
    }

    @GetMapping("checkJwt")
    public ResponseEntity<Object> checkJwt(HttpServletRequest request,
                                           @RequestParam String jwt) {
        User account = accountService.checkJwt(jwt);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/signup-check")
    public ResponseEntity<Boolean> doSocialLogin(@RequestBody UserParam userParam) {
        boolean result = this.accountService.isExistAccount(userParam.getLoginId(), AccountType.valueOf(userParam.getAccountType()));
        if(result) {
            return ResponseEntity.ok(true);
        }
        return ResponseEntity.ok(false);
    }


    /**
     * 로그인
     *
     * @param params
     * @param request
     * @return
     * @throws Exception
     */
//    @SuppressWarnings("unused")
//    @PostMapping(value = "/api/login")
//    public ResponseEntity<SessionDTO> login(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response)
//            throws Exception {
//
//        SessionDTO sessionDto = new SessionDTO();
//        HttpSession session = request.getSession(true);
//
//        String userId = params.get("userId").toString();
//        String pwd = params.get("pwd").toString();
//
//        String ipAddr = request.getHeader("X-FORWARDED-FOR");
//        if (ipAddr == null) {
//            ipAddr = request.getRemoteAddr();
//        }
//
//        // 유저 정보 호출
////        Account loginDto = lgnService.getUser(userId);
//
//        // 유저 정보가 없을시
//        if (loginDto == null) {
//            return new ResponseEntity<SessionDTO>(failMessage("계정이 존재하지 않습니다."), HttpStatus.OK);
//        }
//
//        // 비밀번호 변경 주기 체크
////		if (false) {
////			return new ResponseEntity<SessionDTO>(failMessage("비밀번호 변경 주기가 지났습니다. 비밀번호를 변경해 주세요."), HttpStatus.OK);
////		}
//
//        // 비밀번호 실패 횟수 초과 시
////		if (loginDto.getLgnFailCnt() >= MAX_LOGIN_FAIL_CNT) {
////			return new ResponseEntity<SessionDTO>(failMessage("비밀번호가 " + MAX_LOGIN_FAIL_CNT + "회이상 실패하였습니다."),
////					HttpStatus.OK);
////		}
//
//        // 테스트용 로그인 비밀번호 설정
//        //loginDto.setUserPswd(pwd);
//        //loginDto.hashPassword(bCryptPasswordEncoder);
//        if (loginDto.checkPassword(pwd, bCryptPasswordEncoder)) {// 성공
//            // 로그인 성공 상태 DB 업데이트
//            //lgnService.updateUserLgnSucess(userId, ipAddr);
//
//            // 로그인 완료 후 세션 설정
//            sessionDto.setSession_usrid(loginDto.getUserId());
//            sessionDto.setSession_usrname(loginDto.getUserNm());
//            sessionDto.setSession_instcd(loginDto.getInstCd());
//            sessionDto.setSession_instname(loginDto.getInstNm());
//            sessionDto.setSession_orgid(loginDto.getDeptCd());
//            sessionDto.setSession_orgname(loginDto.getDeptNm());
//            sessionDto.setSession_message("정상적으로 로그인 되었습니다.");
//            session.setMaxInactiveInterval(SESSION_TIME_SEC);// 세션 타임아웃 설정 30분(초단위)
//
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> sessionMap = mapper.convertValue(sessionDto, Map.class);
//
//            session.setAttribute(SESSION_NAME, sessionMap);
//        } else {// 실패
//            // 로그인 실패 횟수 DB 업데이트
//            //lgnService.updateUserLgnFail(userId, ipAddr);
//            // return new ResponseEntity<SessionDTO>(
//            // 					failMessage("비밀번호가 올바르지 않습니다. (" + (loginDto.getLgnFailCnt() + 1) + "/" + MAX_LOGIN_FAIL_CNT + ")"),
//            // 					HttpStatus.OK);
//            return new ResponseEntity<SessionDTO>(
//                    failMessage("비밀번호가 올바르지 않습니다."),
//                    HttpStatus.OK);
//        }
//
//        return new ResponseEntity<SessionDTO>(sessionDto, HttpStatus.OK);
//    }

//    @PostMapping(value = "/api/logout")
//    public ResponseEntity<Integer> logout(@RequestBody HashMap<String, Object> params, HttpServletRequest request, HttpServletResponse response)
//            throws Exception {
//        HttpSession session = request.getSession(true);
//        Map<String, Object> sessionMap = new HashMap<String, Object>();
//
//        if(request != null && request.getSession(true) != null) {
//            sessionMap = (Map<String, Object>) session.getAttribute("SessionDTO");
//            if(sessionMap == null) sessionMap = new HashMap<String, Object>();
//            LOGGER.debug("==================removeSession==============" + sessionMap);
//            session.removeAttribute(SESSION_NAME);
//            session.invalidate();
//            new ResponseEntity<Integer>(1, HttpStatus.OK);
//        }
//        return new ResponseEntity<Integer>(0, HttpStatus.OK);
//
//    }
}
