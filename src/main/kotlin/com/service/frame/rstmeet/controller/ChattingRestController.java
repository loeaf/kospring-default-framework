package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.params.ChatParam;
import com.service.frame.rstmeet.model.Chatting;
import com.service.frame.rstmeet.model.TasteRoom;
import com.service.frame.rstmeet.service.ChattingService;
import com.service.frame.rstmeet.service.TasteRoomService;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Date;

@RestController
@RequestMapping("/Chatting")
public class ChattingRestController {

    private ChattingService service;
    private TasteRoomService tasteRoomService;

    public ChattingRestController(ChattingService service, TasteRoomService tasteRoomService) {
        this.service = service;
        this.tasteRoomService = tasteRoomService;
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll(HttpServletRequest request, Pageable pageable, @RequestParam String roomId) throws Exception {
        return ResponseEntity.ok(service.findById(roomId));
    }

    @PostMapping()
    public ResponseEntity<Object> regist(HttpServletRequest request,
                                         @RequestBody ChatParam chatParam) throws Exception {
        Chatting chatting = new Chatting();
        chatting.setContent(chatParam.getContent());
        TasteRoom tasteRoom = tasteRoomService.findById(chatParam.getTasteRoomId());
        chatting.setTasteRoom(tasteRoom);
        chatting.setId(chatParam.getUserId());
        chatting.setCreateDate(new Date());
        return ResponseEntity.ok(service.regist(chatting));
    }
}