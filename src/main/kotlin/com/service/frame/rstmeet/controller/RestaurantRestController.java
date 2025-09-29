package com.service.frame.rstmeet.controller;

import com.service.frame.datacreator.service.InstaInfoService;
import com.service.frame.file.domain.FileInfo;
import com.service.frame.file.service.FileInfoService;
import com.service.frame.rstmeet.dto.ResResult;
import com.service.frame.rstmeet.dto.params.MediaParam;
import com.service.frame.rstmeet.dto.params.RestaurantParam;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.RestaurantDto;
import com.service.frame.rstmeet.service.RestaurantService;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/Restaurant")
public class RestaurantRestController {

    private RestaurantService service;
    private InstaInfoService instaInfoService;
    private FileInfoService fileInfoService;
    @Value("${app.file.upload.img-path}")
    private String uploadDir;
    Logger logger = LoggerFactory.getLogger(RestaurantRestController.class);

    public RestaurantRestController(RestaurantService service,
                                    InstaInfoService instaInfoService,
                                    FileInfoService fileInfoService) {
        this.service = service;
        this.instaInfoService = instaInfoService;
        this.fileInfoService = fileInfoService;
    }

    @PostMapping()
    public ResponseEntity<ResResult> findAll(HttpServletRequest request, @RequestBody RestaurantParam params) throws Exception {
        ResResult resResult = new ResResult();
        logger.info("findAll ========> " + params.toString());
        List<RestaurantDto> restaurantList = service.findRestaurant(params);
        resResult.setData(restaurantList);
        return ResponseEntity.ok(resResult);
    }
    @GetMapping("/findRestByInstaId")
    public ResponseEntity<ResResult> findAllByInstaId(HttpServletRequest request, @RequestParam String instaInfoId) throws Exception {
        ResResult resResult = new ResResult();
        List<RestaurantDto> restaurantList = service.findRestaurantByInstaInfoId(instaInfoId);
        resResult.setData(restaurantList);
        return ResponseEntity.ok(resResult);
    }

    @PostMapping("/regist")
    @Transactional
    public ResponseEntity<Object> regist(HttpServletRequest request, @RequestBody RestaurantParam dto) throws Exception {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(UUID.randomUUID().toString());
        restaurant.setName(dto.getName());
        restaurant.setRoadAddress(dto.getRoadAddress());
        restaurant.setJibunAddress(dto.getJibunAddress());
        restaurant.setPhoneNumber(dto.getPhoneNumber());
        restaurant.setEnglishAddress(dto.getEnglishAddress());
        restaurant.setMiniAddress(dto.getMiniAddress());
        restaurant.setReferenceUrl(dto.getReferenceUrl());
        restaurant.setRepresentativeMenu(dto.getRepresentativeMenu());
        restaurant.setRegDate(new Date());
        var instaInfo = this.instaInfoService.findById(dto.getInstaIdRef());
        restaurant.setInstaInfo(instaInfo);
        service.regist(restaurant);
        return null;
    }

    @PostMapping("/fetch")
    @Transactional
    public ResponseEntity<Object> fetch(HttpServletRequest request, @RequestPart RestaurantParam dto, @RequestPart(name="file", required = false) MultipartFile file) throws Exception {
        if (file != null) {
            List<FileInfo> result = this.fileInfoService.procCPFiles(new MultipartFile[]{file});
            MediaParam media = new MediaParam();
            media.setId(UUID.randomUUID().toString());
            media.setName(result.get(0).getFileName());
            media.setNormalFileName(result.get(0).getNormalFileName());
            media.setSmallFileName(result.get(0).getSmallFileName());
            media.setPath(result.get(0).getFilePath());
            dto.setMedia(media);
        }
        service.fetch(dto);
        ResResult resResult = new ResResult();
        resResult.setData(1);
        return ResponseEntity.ok(resResult);
    }

    @PostMapping("/fetchSumnail")
    @Transactional
    public ResponseEntity<Object> fetchSumnail(HttpServletRequest request, @RequestBody RestaurantParam dto) throws Exception {
        List<FileInfo> result = this.fileInfoService.procCPFilesByRestparam(dto);
        MediaParam media = new MediaParam();
        media.setId(UUID.randomUUID().toString());
        media.setName(result.get(0).getFileName());
        media.setNormalFileName(result.get(0).getNormalFileName());
        media.setSmallFileName(result.get(0).getSmallFileName());
        media.setPath(result.get(0).getFilePath());
        dto.setMedia(media);
        service.fetch(dto);
        ResResult resResult = new ResResult();
        resResult.setData(1);
        return ResponseEntity.ok(resResult);
    }
}