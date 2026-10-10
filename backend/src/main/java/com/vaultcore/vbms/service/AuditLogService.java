package com.vaultcore.vbms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.AuditLogDtos.AuditLogResponse;
import com.vaultcore.vbms.entity.TransactionLog;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.TransactionLogRepository;

@Service
public class AuditLogService {
    private final TransactionLogRepository logs;

    public AuditLogService(TransactionLogRepository logs) {
        this.logs = logs;
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> list(Integer performedBy, String action, LocalDateTime from, LocalDateTime to) {
        return logs.findForMonitoring(performedBy, action, from, to).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public AuditLogResponse get(Integer logId) {
        TransactionLog log = logs.findById(logId)
                .orElseThrow(() -> new NotFound("Audit log not found"));
        return response(log);
    }

    private AuditLogResponse response(TransactionLog log) {
        return new AuditLogResponse(
                log.getLogId(),
                log.getTransaction().getTransactionId(),
                log.getPerformedBy(),
                log.getAction(),
                log.getLogTime(),
                log.getRemarks());
    }
}
