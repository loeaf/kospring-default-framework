package com.service.frame.rstmeet.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.rstmeet.mapper.TasteRoomMapper;
import com.service.frame.rstmeet.model.Restaurant;
import com.service.frame.rstmeet.model.TasteRoom;
import com.service.frame.rstmeet.repository.TasteRoomRepository;
import com.service.frame.rstmeet.service.TasteRoomService;
import com.service.frame.siginin.dto.UserToken;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.service.AccountService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TasteRoomServiceImpl
        extends ServiceImpl<TasteRoomRepository, TasteRoom, String>
        implements TasteRoomService {
    private final TasteRoomRepository jpaRepo;
    @Autowired
    UserToken userToken;
    @Autowired
    AccountService accountService;

    @Autowired
    TasteRoomMapper tasteRoomMapper;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new TasteRoom());
    }

    @Override
    public List<TasteRoom> findByRestaurant(Restaurant restaurant) {
        System.out.println(userToken.getUser().toString());
        User user = userToken.findUserByDb();
        List<TasteRoom> tasteRooms = this.jpaRepo.findByRestaurantAndUserNot(restaurant, user);
        return tasteRooms;
    }

    @Override
    public List<TasteRoom> selectTasteRoom(String restaurantId) {
        String userId = userToken.getUser().getId();
        List<TasteRoom> result = tasteRoomMapper.findTasteRoomByRestAndNotMe(restaurantId, userId);
        return result;
    }

    @Override
    public List<TasteRoom> findTasteRoomByMe() {
        String userId = userToken.getUser().getId();
        List<TasteRoom> result = tasteRoomMapper.findTasteRoomByMe(userId);
        return result;
    }
}
