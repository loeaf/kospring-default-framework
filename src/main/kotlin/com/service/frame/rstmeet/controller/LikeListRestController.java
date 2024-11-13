package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.dto.params.LikeParam;
import com.service.frame.rstmeet.model.RestaurantDto;
import com.service.frame.rstmeet.service.LikeListService;
import com.service.frame.siginin.dto.UserToken;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/likeList")
public class LikeListRestController {
    @Autowired
    private UserToken userToken;
    private LikeListService service;

    public LikeListRestController(LikeListService service) {
        this.service = service;
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll(HttpServletRequest request, Pageable pageable) throws Exception {
        List<RestaurantDto> restaurantList = this.service.findLikeRestaurant();
        return ResponseEntity.ok(new ResResult(restaurantList));
    }

    /**
     *
     * @param likeParam
     * @return true -> 좋아요, flase -> 좋아요 해제
     * @throws Exception
     */
    @PostMapping("/toggle")
    public ResponseEntity<ResResult> toggle(@RequestBody LikeParam likeParam) throws Exception {
        boolean state = this.service.likeByMapper(likeParam);
        return ResponseEntity.ok(new ResResult(state));
    }
}