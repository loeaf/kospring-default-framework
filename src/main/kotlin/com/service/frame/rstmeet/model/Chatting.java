package com.service.frame.rstmeet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import com.service.frame.siginin.model.User;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity(name = "Chatting")
@AllArgsConstructor
@NoArgsConstructor
public class Chatting extends Domain {
    @Column
    private String content;

    @Column
    private Date createDate;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private TasteRoom tasteRoom;

    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private User user;

}