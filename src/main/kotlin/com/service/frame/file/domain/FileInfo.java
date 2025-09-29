package com.service.frame.file.domain;

import com.service.frame.common.domain.Domain;
import com.service.frame.common.misc.Action;
import com.service.frame.file.domain.listener.FileInfoListener;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * 파일에 대한 기본 정보를 구현하는 Model
 */
@Entity
@EntityListeners(FileInfoListener.class)
@Table(name="file_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo extends Domain implements Action {
    /**
     * 파일명
     */
    @NotNull
    @Column(name = "file_name")
    private String fileName;

    /**
     * 파일을 읽을때 식별하기 위한 폴더
     */
    @NotNull
    @Column(name = "file_path")
    private String filePath;
    /**
     * 임시로 파일을 떨어틀어놓을 경로
     */
    @NotNull
    private String tmpPath;
    /**
     * home에서 부터 target까지의 경로
     */
    private String localPath;

    /**
     * 파일경로
     */
    @NotNull
    @Column(name = "origin_file_name")
    private String originFileName;

    /**
     * 확장자
     */
    @NotNull
    @Column(name = "file_ext")
    private String fileExtention;

    private String smallFileName;

    private String NormalFileName;

    @Override
    public String toString() {
        return this.getFilePath() + "/" + this.getFileName() + "." + this.getFileExtention();
    }

    @Override
    public void delete() {
        new File(this.toString()).delete();
    }

}
