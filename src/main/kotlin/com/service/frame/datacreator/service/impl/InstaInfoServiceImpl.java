package com.service.frame.datacreator.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.datacreator.dto.params.InstaInfoParam;
import com.service.frame.datacreator.mapper.InstaInfoMapper;
import com.service.frame.datacreator.model.InstaInfo;
import com.service.frame.datacreator.repository.InstaInfoRepository;
import com.service.frame.datacreator.service.InstaInfoService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstaInfoServiceImpl
        extends ServiceImpl<InstaInfoRepository, InstaInfo, String>
        implements InstaInfoService {
    private final InstaInfoRepository jpaRepo;

    @Autowired
    InstaInfoMapper mapper;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new InstaInfo());
    }

    @Override
    public List<InstaInfo> selectByMybatis(InstaInfoParam param) {
        return this.mapper.selectByPrimaryKey(param);
    }
}
