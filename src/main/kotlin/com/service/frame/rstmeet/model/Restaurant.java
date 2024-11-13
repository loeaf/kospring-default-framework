package com.service.frame.rstmeet.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import com.service.frame.datacreator.model.InstaInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

import java.util.Date;
import java.util.List;

@Data
@Entity(name = "Restaurant")
@AllArgsConstructor
@NoArgsConstructor
public class Restaurant extends Domain {
    // 맛집번호
    @Column
    private Integer restaurantNumber;
    // 맛집명
    @Column
    private String name;
    // 한글도로명주소
    @Column
    private String roadAddress;
    // 한글지번주소
    @Column
    private String jibunAddress;
    // 영어주소
    @Column
    private String englishAddress;
    // 요약주소
    @Column
    private String miniAddress;
    // 위도
    @Column
    private Double latitude;
    // 경도
    @Column
    private Double longitude;
    // 지리정보
    /**
     * Geometry 타입 지원틀 위해서 수동으로 db에 해당 타입을 만들어주어야함.
     */
    @Transient
    @Column(name = "geoInfo")
    private Point geoInfo;
    // 등록일
    @Column
    private Date regDate;
    // 수정일
    @Column
    private Date updateDate;
    // 전화번호
    @Column
    private String phoneNumber;
    // 휴무일
    @Column
    private String holiday;
    // 구글 콜 주소
    @Column
    private String googleCallApiUrl;
    // 구글 데이터 uuid
    @Column
    private String googleCallDataUuid;
    // 참고URL
    @Column(length = 2024)
    private String referenceUrl;
    // 인스타그램 아이디 참고 값
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private InstaInfo instaInfo;
    // 대표메뉴
    @Column
    private String representativeMenu;

    @Column(columnDefinition = "int default 0")
    private Integer refinedGeoLocation;
    // 대표메뉴
    @Column
    private String dataType;
    // 대표메뉴
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private CmmnCode foodType;
    // 국가구분코드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private CmmnCode countryType;
    // 도시구분코드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private CmmnCode cityType;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Media> medias;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Menu> menus;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ReView> reViews;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<TasteRoom> tasteRooms;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LikeList> restaurantTags;
}