package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.rstmeet.model.TasteRoomMember;
import com.service.frame.rstmeet.repository.TasteRoomMemberRepository;
import com.service.frame.rstmeet.service.TasteRoomMemberService;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TasteRoomMemberServiceImpl
        extends ServiceImpl<TasteRoomMemberRepository, TasteRoomMember, String>
        implements TasteRoomMemberService {
    private final TasteRoomMemberRepository jpaRepo;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new TasteRoomMember());
    }
}
