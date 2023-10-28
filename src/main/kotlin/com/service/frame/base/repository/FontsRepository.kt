package com.service.frame.base.repository

import com.service.frame.base.entity.Fonts
import org.springframework.data.jpa.repository.JpaRepository

interface FontsRepository : JpaRepository<Fonts, Long>