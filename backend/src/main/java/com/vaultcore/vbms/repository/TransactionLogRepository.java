package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionLogRepository extends JpaRepository<TransactionLog,Integer> {
    default List<TransactionLog> findForMonitoring(
            Integer performedBy, String action, LocalDateTime startTime, LocalDateTime endTime) {
        return findForMonitoringFiltered(
                performedBy == null ? -1 : performedBy,
                action == null || action.isBlank() ? "" : action.trim(),
                startTime == null ? LocalDateTime.of(1900, 1, 1, 0, 0) : startTime,
                endTime == null ? LocalDateTime.of(2999, 12, 31, 23, 59, 59) : endTime);
    }

    @Query("""
            select log from TransactionLog log
            where (:performedBy = -1 or log.performedBy = :performedBy)
              and (:action = '' or lower(log.action) = lower(:action))
              and log.logTime >= :startTime
              and log.logTime <= :endTime
            order by log.logTime desc
            """)
    List<TransactionLog> findForMonitoringFiltered(
            @Param("performedBy") Integer performedBy,
            @Param("action") String action,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
