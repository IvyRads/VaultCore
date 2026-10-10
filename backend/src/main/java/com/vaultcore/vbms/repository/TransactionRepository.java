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

    default List<Transaction> findForMonitoring(
            String transactionType, Integer accountId, LocalDateTime startDate, LocalDateTime endDate) {
        return findForMonitoringFiltered(
                transactionType == null || transactionType.isBlank() ? "" : transactionType.trim(),
                accountId == null ? -1 : accountId,
                startDate == null ? LocalDateTime.of(1900, 1, 1, 0, 0) : startDate,
                endDate == null ? LocalDateTime.of(2999, 12, 31, 23, 59, 59) : endDate);
    }

    @Query("""
            select t from Transaction t
            where (:transactionType = '' or lower(t.transactionType) = lower(:transactionType))
              and (:accountId = -1 or t.account.accountId = :accountId)
              and t.transactionDate >= :startDate
              and t.transactionDate <= :endDate
            order by t.transactionDate desc
            """)
    List<Transaction> findForMonitoringFiltered(
            @Param("transactionType") String transactionType,
            @Param("accountId") Integer accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
