package com.service.frame.rstmeet.repository;

import com.service.frame.rstmeet.model.Chatting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRepository extends JpaRepository<Chatting, String> {
}