package com.service.frame.rstmeet.repository;

import com.service.frame.rstmeet.model.CmmnCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CmmnCodeRepository extends JpaRepository<CmmnCode, String> {
    List<CmmnCode> findCmmnCodeByParentCode(CmmnCode cd);

    CmmnCode findByCodeName(String menuType);
}