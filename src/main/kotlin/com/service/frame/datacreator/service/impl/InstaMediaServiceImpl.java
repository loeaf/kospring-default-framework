package com.service.frame.datacreator.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.datacreator.mapper.InstaMediaMapper;
import com.service.frame.datacreator.model.InstaMedia;
import com.service.frame.datacreator.repository.InstaMediaRepository;
import com.service.frame.datacreator.service.InstaMediaService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstaMediaServiceImpl
        extends ServiceImpl<InstaMediaRepository, InstaMedia, String>
        implements InstaMediaService {
    private final InstaMediaRepository jpaRepo;

    @Autowired
    InstaMediaMapper mapper;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new InstaMedia());
    }

    @Override
    public List<InstaMedia> findByInstaInfoId(String id) {
        List<InstaMedia> instaMediaList = this.jpaRepo.findByInstaInfoId(id);
        return instaMediaList;
    }
}
