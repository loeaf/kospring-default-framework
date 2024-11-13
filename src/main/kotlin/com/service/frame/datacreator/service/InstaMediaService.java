package com.service.frame.datacreator.service;

import com.service.frame.common.misc.Service;
import com.service.frame.datacreator.model.InstaMedia;

import java.util.List;

public interface InstaMediaService extends Service<InstaMedia, String> {
    List<InstaMedia> findByInstaInfoId(String id);
}
