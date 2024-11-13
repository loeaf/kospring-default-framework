package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.rstmeet.model.Chatting;
import com.service.frame.rstmeet.repository.ChattingRepository;
import com.service.frame.rstmeet.service.ChattingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChattingServiceImpl
        extends ServiceImpl<ChattingRepository, Chatting, String>
        implements ChattingService {
    private final ChattingRepository jpaRepo;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new Chatting());
    }
}
