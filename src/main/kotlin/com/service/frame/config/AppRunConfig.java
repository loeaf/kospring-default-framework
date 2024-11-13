package com.service.frame.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AppRunConfig implements ApplicationRunner {

    @Value("${app.file.upload.img-path}")
    private String imgPath;

    @Value("${app.file.upload.google-info-path}")
    private String googleInfoPath;

    @Value("${app.file.tmp.path}")
    private String tmpPath;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createFolderIfNotExists(imgPath);
        createFolderIfNotExists(googleInfoPath);
        createFolderIfNotExists(tmpPath);
    }

    private void createFolderIfNotExists(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            boolean result = folder.mkdirs();
            if (!result) {
                throw new RuntimeException("Failed to create folder: " + path);
            }
        }
    }
}