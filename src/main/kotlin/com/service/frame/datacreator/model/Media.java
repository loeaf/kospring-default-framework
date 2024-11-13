package com.service.frame.datacreator.model;

import com.service.frame.common.domain.Domain;
import lombok.Data;

import java.util.Date;

@Data
public class Media extends Domain {

    private String name;

    private String normalFileName;

    private String path;

    private Date regDate;

    private String smallFileName;

    private String restaurantId;

    private String instagramId;

    private String shortCode;
}