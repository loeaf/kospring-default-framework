package com.service.frame.siginin.model;

import com.service.frame.common.domain.Domain;
import com.service.frame.common.misc.BizField;
import com.service.frame.siginin.types.Authority;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role extends Domain {
    @Column(length = 10, nullable = false, unique = true)
    @Enumerated(EnumType.STRING)
    @BizField
    private Authority authority;
}
