package com.service.frame.datacreator.repository;

import com.service.frame.datacreator.model.InstaInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstaInfoRepository extends JpaRepository<InstaInfo, String> {
}