package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLogRepository extends JpaRepository<TransactionLog,Integer> {
    @Query("""
            select log from TransactionLog log
            where (:performedBy is null or log.performedBy = :performedBy)
              and (:action is null or lower(log.action) = lower(:action))
              and (:startTime is null or log.logTime >= :startTime)
              and (:endTime is null or log.logTime <= :endTime)
            order by log.logTime desc
            """)
    List<TransactionLog> findForMonitoring(
            @Param("performedBy") Integer performedBy,
            @Param("action") String action,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
