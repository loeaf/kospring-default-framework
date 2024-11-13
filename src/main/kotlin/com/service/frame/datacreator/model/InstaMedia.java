package com.service.frame.datacreator.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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