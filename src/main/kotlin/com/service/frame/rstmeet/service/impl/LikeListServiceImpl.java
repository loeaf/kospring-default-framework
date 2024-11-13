package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.rstmeet.dto.params.LikeParam;
import com.service.frame.rstmeet.mapper.LikeListMapper;
import com.service.frame.rstmeet.model.LikeList;
import com.service.frame.rstmeet.model.RestaurantDto;
import com.service.frame.rstmeet.repository.LikeListRepository;
import com.service.frame.rstmeet.service.LikeListService;
import com.service.frame.siginin.dto.UserToken;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeListServiceImpl
        extends ServiceImpl<LikeListRepository, LikeList, String>
        implements LikeListService {
    private final LikeListRepository jpaRepo;
    @Autowired
    private UserToken userToken;
    @Autowired
    LikeListMapper likeListMapper;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new LikeList());
    }

    @Override
    public boolean likeByMapper(LikeParam likeParam) {
        LikeList likeList = this.likeListMapper.findByUserIdAndRestaurantId(userToken.getUser().getId(), likeParam.getRestaurantId());
        if (likeList == null) { // 좋아요
            this.likeListMapper.regist(
                    UUID.randomUUID().toString(),
                    userToken.getUser().getId(),
                    likeParam.getRestaurantId());
            return true;
        } else { // 좋아요 제거
            this.jpaRepo.delete(likeList);
            return false;
        }
    }

    @Override
    public List<RestaurantDto> findLikeRestaurant() {
        List<RestaurantDto> restaurantList = this.likeListMapper.findLikeRestaurant(userToken.getUser().getId());
        return restaurantList;
    }
}
