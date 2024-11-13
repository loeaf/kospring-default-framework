package com.service.frame.siginin.service.impl;

import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.exception.DuplicateDataException;
import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.service.AccountService;
import com.service.frame.siginin.service.RoleService;
import com.service.frame.siginin.service.SigininService;
import com.service.frame.siginin.service.UserService;
import com.service.frame.siginin.types.AccountType;
import com.service.frame.siginin.util.JwtManager;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SigininServiceImpl implements SigininService {
    @Autowired
    final UserService userService;
    @Autowired
    final AccountService accountService;
    @Autowired
    final RoleService roleService;
    @Autowired
    final JwtManager jwtManager;
    final PasswordEncoder passwordEncoder;


    @Transactional
    public User save(UserParam userParam) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
//        checkDuplication(userParam);
        User user = new User();
//        Set<Role> roles = new HashSet<>();
//        roles.add(roleService.findByBizKey(Role.builder().authority(Authority.USER).build()));
//        user.setRoles(roles);
        user.setId(UUID.randomUUID().toString());
        user.setNickName(userParam.getNickName());
        var userObj = userService.regist(user);
        Account account = new Account();
        account.setType(AccountType.valueOf(userParam.getAccountType()));
        account.setId(UUID.randomUUID().toString());
        account.setLoginId(userParam.getLoginId());
        account.setPassword(passwordEncoder.encode(userParam.getPassword()));
        account.setUser(userObj);
        var accountObj = accountService.regist(account);
        accountObj.setPassword(null);
        return user;
    }

    public String signUp(UserParam userParam) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        var account = this.save(userParam);
        return jwtManager.generateJwtToken(account);
    }

    @SneakyThrows
    private void checkDuplication(UserParam userParam) {
        AccountType accountType = AccountType.valueOf(userParam.getAccountType());
        Account existsEmail = accountService.findByLoginIdAndType(userParam.getLoginId(), accountType);
        if (existsEmail != null) {
            throw new DuplicateDataException(existsEmail.toString());
        }
        User user = userService.findByNickName(userParam.getNickName());
        if(user != null) {
            var existsNick = userService.findByNickName(user.getNickName());
            if (existsNick != null) {
                throw new DuplicateDataException(user.toString());
            }
        }
    }
}
