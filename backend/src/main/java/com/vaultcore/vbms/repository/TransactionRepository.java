package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.*;
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {
    List<Transaction> findByAccountAccountIdOrderByTransactionDateDesc(Integer accountId);
    List<Transaction> findByAccountCustomerCustomerIdOrderByTransactionDateDesc(Integer customerId);

    @Query("""
            select t from Transaction t
            where (:transactionType is null or lower(t.transactionType) = lower(:transactionType))
              and (:accountId is null or t.account.accountId = :accountId)
              and (:startDate is null or t.transactionDate >= :startDate)
              and (:endDate is null or t.transactionDate <= :endDate)
            order by t.transactionDate desc
            """)
    List<Transaction> findForMonitoring(
            @Param("transactionType") String transactionType,
            @Param("accountId") Integer accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
