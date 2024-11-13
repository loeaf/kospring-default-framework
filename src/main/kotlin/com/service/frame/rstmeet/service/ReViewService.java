package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.model.ReView;

import java.io.FileNotFoundException;

public interface ReViewService extends Service<ReView, String> {
    void registBulkByCSV() throws FileNotFoundException;
}
