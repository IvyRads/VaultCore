package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TransactionRepository extends JpaRepository<Transaction,Integer> { List<Transaction> findByAccountAccountIdOrderByTransactionDateDesc(Integer accountId); List<Transaction> findByAccountCustomerCustomerIdOrderByTransactionDateDesc(Integer customerId); }
