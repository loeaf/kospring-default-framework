package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.service.ReViewService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ReView")
public class ReViewRestController {

    private ReViewService service;

    public ReViewRestController(ReViewService service) {
        this.service = service;
    }

    @GetMapping("")
    public ResponseEntity<Object> findAll(HttpServletRequest request, Pageable pageable) throws Exception {
        return null;
    }

    @PostMapping()
    public ResponseEntity<Object> regist(HttpServletRequest request, @RequestBody Object dto) throws Exception {
        return null;
    }
}