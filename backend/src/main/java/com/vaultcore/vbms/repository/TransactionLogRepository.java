package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TransactionLogRepository extends JpaRepository<TransactionLog,Integer> { }
