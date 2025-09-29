package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.rstmeet.model.CmmnCode;
import com.service.frame.rstmeet.repository.CmmnCodeRepository;
import com.service.frame.rstmeet.service.CmmnCodeService;
import com.service.frame.rstmeet.type.CountryType;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CmmnCodeServiceImpl
        extends ServiceImpl<CmmnCodeRepository, CmmnCode, String>
        implements CmmnCodeService {
private final CmmnCodeRepository jpaRepo;

@PostConstruct
private void init(){
        super.set(jpaRepo,new CmmnCode());
        }

    @Override
    public List<CmmnCode> findCity(CountryType countryType) {
        var cd = new CmmnCode();
        cd.setId(countryType.getName());
        return jpaRepo.findCmmnCodeByParentCode(cd);
    }

    @Override
    public List<CmmnCode> findNation() {
    var cd = new CmmnCode();
    cd.setId("NATION");
    return jpaRepo.findCmmnCodeByParentCode(cd);
    }
}
