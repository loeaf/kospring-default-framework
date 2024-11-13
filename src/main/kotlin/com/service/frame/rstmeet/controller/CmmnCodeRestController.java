package com.service.frame.rstmeet.controller;

import com.service.frame.file.service.FileInfoService;
import com.service.frame.rstmeet.dto.ResultResponse;
import com.service.frame.rstmeet.service.CmmnCodeService;
import com.service.frame.rstmeet.type.CountryType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/CmmnCode")
public class CmmnCodeRestController {

    private CmmnCodeService service;
    private FileInfoService fileInfoServiceImpl;

    public CmmnCodeRestController(CmmnCodeService service, FileInfoService fileInfoServiceImpl) {
        this.service = service;
        this.fileInfoServiceImpl = fileInfoServiceImpl;
    }

    // get send fileinfo service
    @GetMapping("/getFood")
    public ResponseEntity<ResultResponse> findFood(HttpServletRequest request) throws Exception {
        fileInfoServiceImpl.sendS3Files();
        return ResponseEntity.ok(new ResultResponse(null));
    }

    // NATION TYPE [ KOREA, JAPEN ]
    // find city by service
    @GetMapping("/getCity")
    public ResponseEntity<ResultResponse> findCity(HttpServletRequest request, @RequestParam String nation) throws Exception {
        CountryType countryType = CountryType.valueOf(nation);
        return ResponseEntity.ok(new ResultResponse(service.findCity(countryType)));
    }

    // find nation by service
    @GetMapping("/getNation")
    public ResponseEntity<ResultResponse> findNation(HttpServletRequest request) throws Exception {
        return ResponseEntity.ok(new ResultResponse(service.findNation()));
    }
}