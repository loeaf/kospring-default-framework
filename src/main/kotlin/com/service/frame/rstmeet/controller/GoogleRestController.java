package com.service.frame.rstmeet.controller;

import com.service.frame.rstmeet.dto.GoogleJsonResponse;
import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.service.GoogleApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/google")
public class GoogleRestController {
    @Autowired
    private GoogleApiService service;
    @GetMapping("")
    public ResponseEntity<Object> findAll(@RequestParam() String location, @RequestParam String uuid) throws Exception {
        GoogleJsonResponse text = service.processGoogleSearch(location, uuid);
        return ResponseEntity.ok(new ResResult(text));
    }

}