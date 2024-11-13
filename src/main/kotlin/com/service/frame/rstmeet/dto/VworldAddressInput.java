package com.service.frame.rstmeet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VworldAddressInput {
    private Map<String, String> point;
    private String crs;
    private String type;
}