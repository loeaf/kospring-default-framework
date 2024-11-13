package com.service.frame.datacreator.repository;

import com.service.frame.datacreator.model.InstaMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InstaMediaRepository extends JpaRepository<InstaMedia, String> {
    List<InstaMedia> findByInstaInfoId(String id);
}