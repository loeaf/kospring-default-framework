package com.service.frame.rstmeet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity(name = "CmmnCode")
@AllArgsConstructor
@NoArgsConstructor
public class CmmnCode extends Domain {
    @Column
    private String codeName;
    // one to many parentCode Column
    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private CmmnCode parentCode;

    @OneToMany(mappedBy = "parentCode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CmmnCode> cmmnCodeList;

    @OneToMany(mappedBy = "countryType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Restaurant> restaurantsList;

    @OneToMany(mappedBy = "cityType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Restaurant> cityList;

    @OneToMany(mappedBy = "menuType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Menu> menuList;
}