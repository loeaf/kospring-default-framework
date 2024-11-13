package com.service.frame.file.controller;

import com.service.frame.file.domain.FileInfo;
import com.service.frame.file.service.FileInfoService;
import com.service.frame.rstmeet.dto.ResResult;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/File")
@Api(value = "File")
public class FileController {
    @Autowired
    private FileInfoService service;
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("image") MultipartFile file) {
        List<FileInfo> result = this.service.procCPFiles(new MultipartFile[]{file});
        ResResult resResult = new ResResult();
        resResult.setData(result);
        return ResponseEntity.ok(resResult);
    }
}