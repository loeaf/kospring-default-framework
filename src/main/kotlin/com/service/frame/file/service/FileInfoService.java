package com.service.frame.file.service;

import com.service.frame.common.misc.Service;
import com.service.frame.file.domain.FileInfo;
import com.service.frame.rstmeet.dto.params.RestaurantParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileInfoService extends Service<FileInfo, Long> {
    List<FileInfo> procCPFiles(MultipartFile[] multipartFiles);
    List<FileInfo> procCPFilesByRestparam(RestaurantParam requestParam) throws IOException;
    void sendS3Files() throws IOException;
}
