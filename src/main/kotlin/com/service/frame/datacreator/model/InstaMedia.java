package com.service.frame.datacreator.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity()
@AllArgsConstructor
@NoArgsConstructor
public class InstaMedia extends Domain {
    @Column
    private String filename;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id", name = "insta_info_id")
    @JsonBackReference
    private InstaInfo instaInfo;
}