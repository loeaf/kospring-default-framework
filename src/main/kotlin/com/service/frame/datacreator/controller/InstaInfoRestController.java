package com.service.frame.datacreator.controller;

import com.service.frame.datacreator.dto.params.InstaInfoParam;
import com.service.frame.datacreator.service.InstaInfoService;
import com.service.frame.datacreator.service.InstaMediaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/instaInfo")
@Api(value = "instaInfo")
public class InstaInfoRestController {

    private InstaInfoService service;
    private InstaMediaService instaMediaService;

    public InstaInfoRestController(InstaInfoService service,
                                   InstaMediaService instaMediaService) {
        this.service = service;
        this.instaMediaService = instaMediaService;
    }

    @PostMapping("find")
    @ApiOperation(value = "기본 전체목록")
    public ResponseEntity<Object> findAll(HttpServletRequest request, @RequestBody InstaInfoParam param) throws Exception {
        if(param.getId().equals("")) {
            return ResponseEntity.ok(service.selectByMybatis(param));
        } else {
            var obj = service.findById(param.getId());
//            obj.setInstaMediaList(instaMediaService.findByInstaInfoId(obj.getId()));
            return ResponseEntity.ok(obj);
        }
    }

    @GetMapping("finish")
    @ApiOperation(value = "기본 전체목록")
    public ResponseEntity<Object> finish(HttpServletRequest request, @RequestParam String uuid) throws Exception {
        var instaInfo = this.service.findById(uuid);
        if (instaInfo != null) {
            instaInfo.setIsWorked(1);
            instaInfo = this.service.regist(instaInfo);
        }
        return ResponseEntity.ok(instaInfo);
    }

    @PostMapping()
    @ApiOperation(value = "등록")
    public ResponseEntity<Object> regist(HttpServletRequest request, @RequestBody Object dto) throws Exception {
        return null;
    }
}