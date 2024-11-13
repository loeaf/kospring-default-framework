package com.service.frame.siginin.repository;

import com.service.frame.siginin.model.Account;
import com.service.frame.siginin.types.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
    Account findByLoginIdAndPassword(String loginId, String password);
    Account findByLoginIdAndType(String loginId, AccountType type);
}