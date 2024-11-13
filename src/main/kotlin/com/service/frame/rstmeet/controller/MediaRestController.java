package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.params.RestaurantParam;
import com.service.frame.rstmeet.service.MediaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Media")
public class MediaRestController {

    private MediaService service;

    public MediaRestController(MediaService service) {
        this.service = service;
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll(HttpServletRequest request, Pageable pageable) throws Exception {
        return null;
    }

    @PostMapping()
    public ResponseEntity<Object> regist(HttpServletRequest request, @RequestBody RestaurantParam dto) throws Exception {
        return null;
    }
}