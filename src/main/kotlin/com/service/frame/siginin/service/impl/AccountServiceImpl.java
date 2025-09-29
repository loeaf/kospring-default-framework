package com.service.frame.siginin.service.impl;

import com.service.frame.common.misc.ServiceImpl;
import com.service.frame.siginin.dto.param.UserParam;
import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.model.User;
import com.service.frame.siginin.repository.AccountRepository;
import com.service.frame.siginin.service.AccountService;
import com.service.frame.siginin.types.AccountType;
import com.service.frame.siginin.util.JwtManager;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl
        extends ServiceImpl<AccountRepository, Account, String>
        implements AccountService {
    private final AccountRepository jpaRepo;
    final PasswordEncoder passwordEncoder;
    @Autowired
    JwtManager jwtManager;

    @PostConstruct
    private void init() {
        super.set(jpaRepo, new Account());
    }

    @Override
    public Account findByLoginIdAndType(String loginId, AccountType type) {
        return jpaRepo.findByLoginIdAndType(loginId, type);
    }

    @Override
    public String login(UserParam userForm) {
        // 아이디와 비밀번호 확인
        if(userForm.getLoginId() == null || userForm.getPassword() == null) {
            return null;
        }
        // kakao login
        if(userForm.getAccountType().equals(AccountType.KAKAO.getValue())) {
            Account account = this.jpaRepo.findByLoginIdAndType(userForm.getLoginId(), AccountType.KAKAO);
            return jwtManager.generateJwtToken(account.getUser());
        } else if(userForm.getAccountType().equals(AccountType.EMAIL.getValue())) {
            Account account = this.jpaRepo.findByLoginIdAndType(userForm.getLoginId(), AccountType.EMAIL);
            if(passwordEncoder.matches(userForm.getPassword(), account.getPassword())) {
                return jwtManager.generateJwtToken(account.getUser());
            } else {
                return null;
            }
        } else {
            return null;
        }

    }

    @Override
    public User checkJwt(String jwt) {
        var result = jwtManager.getAccountByToken(jwt);
        return result;
    }

    @Override
    public boolean isExistAccount(String loginId, AccountType accountType) {
        if (jpaRepo.findByLoginIdAndType(loginId, accountType) != null) {
            return true;
        }
        return false;
    }
}
