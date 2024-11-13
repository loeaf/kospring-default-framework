package com.service.frame.rstmeet.repository;

import com.service.frame.rstmeet.model.TasteRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasteRoomMemberRepository extends JpaRepository<TasteRoomMember, String> {
}