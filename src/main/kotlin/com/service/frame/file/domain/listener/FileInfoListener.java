package com.service.frame.file.domain.listener;


import com.service.frame.file.domain.FileInfo;
import jakarta.persistence.PreRemove;


public class FileInfoListener {
    @PreRemove
    public void preRemove(FileInfo fileInfo) {
        fileInfo.delete();
    }
}
