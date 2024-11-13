package com.service.frame.rstmeet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VworldAddressResult {
    private String zipcode;
    private String type;
    private String text;
    private AddressStructure structure;
}