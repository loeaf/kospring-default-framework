package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.model.Media;

import java.io.FileNotFoundException;

public interface MediaService extends Service<Media, String> {
    void registBulkByCSV() throws FileNotFoundException;
}
