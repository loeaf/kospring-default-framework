package com.service.frame.rstmeet.service;

import com.service.frame.rstmeet.dto.GoogleJsonResponse;

public interface GoogleApiService {
    public GoogleJsonResponse getGoogleSearch(String location) throws Exception;
    public GoogleJsonResponse processGoogleSearch(String location, String uuid) throws Exception;
}
