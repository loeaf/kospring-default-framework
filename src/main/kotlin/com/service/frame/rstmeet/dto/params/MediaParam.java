package com.service.frame.rstmeet.dto.params;

import lombok.Data;

@Data
public class MediaParam {
    // 대표메뉴
    private String id;
    private String name;
    private String path;
    private String originFileName;
    private String fileExtention;
    private String smallFileName;
    private String normalFileName;
}