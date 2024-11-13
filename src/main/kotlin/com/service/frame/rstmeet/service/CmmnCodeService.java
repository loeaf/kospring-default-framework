package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.model.CmmnCode;
import com.service.frame.rstmeet.type.CountryType;

import java.util.List;

public interface CmmnCodeService extends Service<CmmnCode, String> {
    List<CmmnCode> findCity(CountryType countryType);
    List<CmmnCode> findNation();
}
