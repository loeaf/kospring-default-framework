package com.service.frame.datacreator.model;

import com.service.frame.common.domain.Domain;
import lombok.Data;

import java.util.Date;

@Data
public class ReView extends Domain {

    private String content;

    private String isMain;

    private Date regDate;

    private String restaurantId;

    private String writerId;
}