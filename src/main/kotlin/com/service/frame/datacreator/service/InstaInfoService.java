package com.service.frame.datacreator.service;

import com.service.frame.common.misc.Service;
import com.service.frame.datacreator.dto.params.InstaInfoParam;
import com.service.frame.datacreator.model.InstaInfo;

import java.util.List;

public interface InstaInfoService extends Service<InstaInfo, String> {
    List<InstaInfo> selectByMybatis(InstaInfoParam param);
}
