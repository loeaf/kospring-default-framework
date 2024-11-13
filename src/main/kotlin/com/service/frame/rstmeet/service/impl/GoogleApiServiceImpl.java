package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.FileUtiles;
import com.service.frame.rstmeet.dto.GoogleJsonResponse;
import com.service.frame.rstmeet.repository.RestaurantRepository;
import com.service.frame.rstmeet.service.GoogleApiService;
import com.service.frame.siginin.util.HttpUtile;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleApiServiceImpl
        implements GoogleApiService {
    @Value("${google.cx}")
    private String googleCxKey;
    @Value("${google.key}")
    private String googleApiKey;
    @Value("${google.url}")
    private String googleApiUrl;
    @Value("${app.file.tmp.path}")
    private String tmpFolder;
    @Value("${app.file.upload.google-info-path}")
    private String targetFolder;
    @Autowired
    private RestaurantRepository restaurantRepository;

    @Override
    public GoogleJsonResponse getGoogleSearch(String location) throws Exception {
        HashMap<String, String> params = new HashMap<>();
        params.put("key", googleApiKey);
        params.put("cx", googleCxKey);
        params.put("q", location);
        params.put("searchType", "IMAGE");
        Object result = HttpUtile.getJsonReponse(googleApiUrl,  params, Object.class);
        // object to file
        String uuid = UUID.randomUUID().toString();
        FileWriter file = new FileWriter(tmpFolder + "/" + uuid + ".json");
        file.write(result.toString());
        file.flush();
        file.close();
        FileUtiles.moveFile(tmpFolder + "/" + uuid + ".json", targetFolder+"/" + uuid + ".json");
        GoogleJsonResponse googleJsonParam = new GoogleJsonResponse();
        googleJsonParam.setUuid(uuid);
        String paramsStr = HttpUtile.buildQueryParams(params);
        googleJsonParam.setJsonText(result.toString());
        googleJsonParam.setGoogleApiUrl(paramsStr);
        return googleJsonParam;
    }

    @Override
    public GoogleJsonResponse processGoogleSearch(String location, String uuid) throws Exception {
        var restaurant = this.restaurantRepository.findById(uuid);
        GoogleJsonResponse googleJsonParam = null;
        if (restaurant.isPresent() && restaurant.get().getGoogleCallDataUuid() != null) {
            var restaurantObj = restaurant.get();
            // readFile and return
            String googleJsonTxt = FileUtiles.readFile(targetFolder + "/" + restaurantObj.getGoogleCallDataUuid() + ".json");
            googleJsonParam = new GoogleJsonResponse();
            googleJsonParam.setUuid(restaurantObj.getGoogleCallDataUuid());
            googleJsonParam.setJsonText(googleJsonTxt);
            googleJsonParam.setGoogleApiUrl(restaurantObj.getGoogleCallApiUrl());
        } else {
            googleJsonParam = this.getGoogleSearch(location);
            var restaurantObj = restaurant.get();
            restaurantObj.setGoogleCallDataUuid(googleJsonParam.getUuid());
            restaurantObj.setGoogleCallApiUrl(googleApiUrl + "?" + googleJsonParam.getGoogleApiUrl());
            this.restaurantRepository.save(restaurantObj);
        }
        return googleJsonParam;
    }

}