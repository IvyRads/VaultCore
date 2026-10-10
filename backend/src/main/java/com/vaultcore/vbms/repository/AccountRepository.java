package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.*;
import com.vaultcore.vbms.dto.AccountManagementDtos.AdminAccountResponse;
public interface AccountRepository extends JpaRepository<Account,Integer> {
    Optional<Account> findByAccountNumber(String number);
    boolean existsByAccountNumber(String number);
    List<Account> findByCustomerCustomerId(Integer customerId);
    long countByStatusIgnoreCase(String status);

    @Query("select a from Account a where a.customer.branch.branchId = :branchId order by a.accountId")
    List<Account> findByCustomerBranchId(@Param("branchId") Integer branchId);

    @Query("select count(a) from Account a where a.customer.branch.branchId = :branchId")
    long countByCustomerBranchId(@Param("branchId") Integer branchId);

    @Query("select coalesce(sum(a.balance), 0) from Account a where a.customer.branch.branchId = :branchId")
    BigDecimal sumBalanceByCustomerBranchId(@Param("branchId") Integer branchId);

    default List<AdminAccountResponse> searchAdminAccounts(
            String accountNumber, String accountType, String status, Integer customerId, Integer branchId,
            Integer accountId) {
        return searchAdminAccountsFiltered(
                accountNumber == null ? "" : accountNumber.trim(),
                accountType == null ? "" : accountType.trim(),
                status == null ? "" : status.trim(),
                customerId == null ? -1 : customerId,
                branchId == null ? -1 : branchId,
                accountId == null ? -1 : accountId);
    }

    @Query("""
            select new com.vaultcore.vbms.dto.AccountManagementDtos$AdminAccountResponse(
                a.accountId, a.accountNumber, a.accountType, a.balance, a.status, a.createdAt,
                c.customerId, c.customerNumber,
                trim(concat(coalesce(p.firstName, ''), concat(' ', coalesce(p.lastName, '')))),
                b.branchId, b.branchName)
            from Account a
            join a.customer c
            left join c.branch b
            left join UserProfile p on p.user.userId = c.user.userId
            where (:accountNumber = '' or lower(a.accountNumber) like lower(concat('%', :accountNumber, '%')))
              and (:accountType = '' or lower(a.accountType) = lower(:accountType))
              and (:status = '' or lower(a.status) = lower(:status))
              and (:customerId = -1 or c.customerId = :customerId)
              and (:branchId = -1 or b.branchId = :branchId)
              and (:accountId = -1 or a.accountId = :accountId)
            order by a.accountId
            """)
    List<AdminAccountResponse> searchAdminAccountsFiltered(
            @Param("accountNumber") String accountNumber,
            @Param("accountType") String accountType,
            @Param("status") String status,
            @Param("customerId") Integer customerId,
            @Param("branchId") Integer branchId,
            @Param("accountId") Integer accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select a from Account a where a.accountId = :id") Optional<Account> findForUpdate(Integer id);
}
