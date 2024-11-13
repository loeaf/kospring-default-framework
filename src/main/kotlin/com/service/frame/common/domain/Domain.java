package com.service.frame.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import jakarta.persistence.Id; // Correct import for JPA entities
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.repository.Temporal;

import java.util.Date;

@Data
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public class Domain {
    @Id
    private String id;

    @CreationTimestamp
    @Column(name = "regist_datetime")
    protected Date registDt;

    @UpdateTimestamp
    @Column(name = "modified_datetime")
    protected Date updtDt;
}
