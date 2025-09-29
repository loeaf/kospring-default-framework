package com.service.frame.datacreator.model;

import com.service.frame.common.domain.Domain;
import com.service.frame.rstmeet.model.Restaurant;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity()
@AllArgsConstructor
@NoArgsConstructor
public class InstaInfo extends Domain {

    @Column(name = "like_info")
    private Integer likeInfo;

    @Column(name = "full_name")
    private String fullName;

    @Column
    private String content;

    private String location;

    @Column
    private String instagramer;

    // 작업여부
    @Column(columnDefinition = "int default 0")
    private Integer isWorked;

    @OneToMany(mappedBy = "instaInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstaMedia> instaMediaList;

    @OneToMany(mappedBy = "instaInfo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Restaurant> restaurants;
}