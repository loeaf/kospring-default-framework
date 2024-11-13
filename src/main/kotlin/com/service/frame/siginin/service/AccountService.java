package com.service.frame.siginin.service;

import com.service.frame.common.misc.Service;
import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.types.AccountType;

public interface AccountService extends Service<Account, String> {
    Account findByLoginIdAndType(String loginId, AccountType type);
    String login(UserParam userForm);
    User checkJwt(String jwt);
    // 이미 가입된 이메일인지 확인
    boolean isExistAccount(String loginId, AccountType type);
}
