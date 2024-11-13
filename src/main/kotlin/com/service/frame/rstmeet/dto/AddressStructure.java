package com.service.frame.rstmeet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressStructure {
    private String level0;
    private String level1;
    private String level2;
    private String level3;
    private String level4L;
    private String level4LC;
    private String level4A;
    private String level4AC;
    private String level5;
    private String detail;
}