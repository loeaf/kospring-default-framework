package com.service.frame.siginin.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.service.frame.common.domain.Domain;
import com.service.frame.siginin.types.AccountType;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "Account",
        uniqueConstraints={
                @UniqueConstraint(
                        name= "AccountUnique",
                        columnNames = {"loginId", "type"}
                )}
        )
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account extends Domain {
    @Column
    private String loginId;
    @Column
    private String password;
    @Column
    private AccountType type;
    @ManyToOne
    @JoinColumn(referencedColumnName = "id")
    @JsonBackReference
    private User user;

}