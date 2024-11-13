package com.service.frame.rstmeet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import com.service.frame.siginin.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity(name = "ReView")
@AllArgsConstructor
@NoArgsConstructor
public class ReView extends Domain {

    // 내용
    @Column
    private String content;

    // 등록일
    @Column
    private Date regDate;

    // 대표리뷰여부
    @Column
    private String isMain;

    // 작성자
    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private User writer;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private Restaurant restaurant;

}