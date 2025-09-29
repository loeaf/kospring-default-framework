package com.service.frame.base.entity

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor


@Entity
@AllArgsConstructor
@NoArgsConstructor
data class CifyFloor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val title: String,
    val status: Int?
) {
    constructor() : this(
        id = null,
        title = "",
        status = null
    ) {

    }
}
