package com.service.frame.base.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import lombok.AllArgsConstructor
import lombok.NoArgsConstructor


@Entity
@AllArgsConstructor
@NoArgsConstructor
data class Fonts(
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
