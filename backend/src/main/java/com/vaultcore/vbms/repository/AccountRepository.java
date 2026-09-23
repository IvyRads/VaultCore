package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface AccountRepository extends JpaRepository<Account,Integer> {
    Optional<Account> findByAccountNumber(String number);
    List<Account> findByCustomerCustomerId(Integer customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.accountId = :id") Optional<Account> findForUpdate(Integer id);
}
