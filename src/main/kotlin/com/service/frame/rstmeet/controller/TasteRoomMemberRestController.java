package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.dto.params.TasteRoomMemberParam;
import com.service.frame.rstmeet.model.TasteRoom;
import com.service.frame.rstmeet.model.TasteRoomMember;
import com.service.frame.rstmeet.service.TasteRoomMemberService;
import com.service.frame.rstmeet.service.TasteRoomService;
import com.service.frame.siginin.dto.UserToken;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("/TasteRoomMember")
public class TasteRoomMemberRestController {

    private TasteRoomMemberService service;
    private TasteRoomService tasteRoomService;
    private AccountService accountService;
    @Autowired
    private UserToken userToken;

    public TasteRoomMemberRestController(TasteRoomMemberService service, TasteRoomService tasteRoomService, AccountService accountService) {
        this.service = service;
        this.tasteRoomService = tasteRoomService;
        this.accountService = accountService;
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll(HttpServletRequest request, Pageable pageable) throws Exception {
        return null;
    }

    @PostMapping()
    public ResponseEntity<ResResult> regist(HttpServletRequest request, @RequestBody TasteRoomMemberParam tasteRoomMemberParam) throws Exception {
        TasteRoomMember tasteRoomMember = new TasteRoomMember();
        tasteRoomMember.setId(UUID.randomUUID().toString());
        TasteRoom tr = this.tasteRoomService.findById(tasteRoomMemberParam.getTasteRoomId());
        tasteRoomMember.setTasteRoom(tr);
//        Account ac = this.accountService.findById(tasteRoomMemberParam.getAccountId());
        User user = userToken.findUserByDb();
        tasteRoomMember.setUser(user);
        tasteRoomMember.setCreateDate(new Date());
        TasteRoomMember result = service.regist(tasteRoomMember);
        return ResponseEntity.ok(new ResResult(result));
    }
}