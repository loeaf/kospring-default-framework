package com.service.frame.rstmeet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.Date;

@Data
@Entity(name = "Media")
@AllArgsConstructor
@NoArgsConstructor
public class Media extends Domain {
    // 파일명
    @Column
    private String name;
    // 썸네일 파일명
    @Column
    private String normalFileName;
    // 썸네일 파일명
    @Column
    private String smallFileName;
    // 인스타그램 아이디
    @Column
    private String instagramId;
    // shortCode
    @Column
    private String shortCode;
    // 경로
    @Column
    private String path;
    @Column
    private Date regDate;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private Restaurant restaurant;
}