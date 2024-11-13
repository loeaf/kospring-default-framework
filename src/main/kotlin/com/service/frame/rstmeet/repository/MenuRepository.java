package com.service.frame.rstmeet.repository;

import com.service.frame.rstmeet.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<Menu, String> {
}