package com.service.frame.siginin.model;

import com.service.frame.common.domain.Domain;
import com.service.frame.rstmeet.model.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity(name = "tn_user")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends Domain {
    @Column
    private String nickName;
//    @ManyToMany
//    @JoinTable(name = "user_role",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id"))
//    private Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TasteRoom> userId;

    @OneToMany(mappedBy = "join", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TasteRoom> joinId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chatting> chattings;
    @OneToMany(mappedBy = "writer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<ReView> reViews;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<TasteRoomMember> attendantTasteRooms;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<LikeList> likeLists;
}
