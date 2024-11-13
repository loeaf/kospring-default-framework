package com.service.frame.rstmeet.service;

import com.service.frame.common.misc.Service;
import com.service.frame.rstmeet.model.Menu;

import java.io.FileNotFoundException;

public interface MenuService extends Service<Menu, String> {
    void registBulkByCSV() throws FileNotFoundException;
}
