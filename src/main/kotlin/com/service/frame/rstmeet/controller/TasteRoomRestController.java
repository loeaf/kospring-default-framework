package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.dto.params.TasteRoomParam;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.TasteRoom;
import com.service.frame.rstmeet.model.TasteRoomMember;
import com.service.frame.rstmeet.service.RestaurantService;
import com.service.frame.rstmeet.service.TasteRoomMemberService;
import com.service.frame.rstmeet.service.TasteRoomService;
import com.service.frame.siginin.dto.UserToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/TasteRoom")
public class TasteRoomRestController {

    private TasteRoomService service;
    private RestaurantService restaurantService;
    private TasteRoomMemberService tasteRoomMemberService;
    @Autowired
    private UserToken userToken;

    public TasteRoomRestController(TasteRoomService service, RestaurantService restaurantService, TasteRoomMemberService tasteRoomMemberService) {
        this.service = service;
        this.restaurantService = restaurantService;
        this.tasteRoomMemberService = tasteRoomMemberService;
    }

    @GetMapping("")
    public ResponseEntity<ResResult> findAll(HttpServletRequest request, Pageable pageable, @RequestParam String restaurantId) throws Exception {
        ResResult resResult = new ResResult();
        List<TasteRoom> tasteRooms = service.selectTasteRoom(restaurantId);
        resResult.setData(tasteRooms);
        return ResponseEntity.ok(resResult);
    }

    @GetMapping("/my")
    public ResponseEntity<ResResult> myTasteRoom(HttpServletRequest request, Pageable pageable) throws Exception {
        ResResult resResult = new ResResult();
        List<TasteRoom> tasteRooms = service.findTasteRoomByMe();
        resResult.setData(tasteRooms);
        return ResponseEntity.ok(resResult);
    }

    @PostMapping()
    @Transactional
    public ResponseEntity<TasteRoom> regist(HttpServletRequest request, @RequestBody TasteRoomParam tasteRoomParam) throws Exception {
        TasteRoom tasteRoom = new TasteRoom();
        tasteRoom.setId(UUID.randomUUID().toString());
        tasteRoom.setContent(tasteRoomParam.getContent());
        Restaurant restaurant = this.restaurantService.findById(tasteRoomParam.getRestaurantId());
        tasteRoom.setRestaurant(restaurant);
        var user = this.userToken.findUserByDb();
        tasteRoom.setUser(user);
        tasteRoom.setCreateDate(new Date());
        tasteRoom.setPeopleNum(tasteRoomParam.getPeopleNum());
        tasteRoom.setMeetPaymentType(tasteRoomParam.getMeetPaymentType());
        TasteRoom result = this.service.regist(tasteRoom);
        TasteRoomMember tasteRoomMember = new TasteRoomMember();
        tasteRoomMember.setId(UUID.randomUUID().toString());
        tasteRoomMember.setTasteRoom(tasteRoom);
        tasteRoomMember.setUser(user);
        tasteRoomMember.setCreateDate(new Date());
        this.tasteRoomMemberService.regist(tasteRoomMember);
        return ResponseEntity.ok(result);
    }
}