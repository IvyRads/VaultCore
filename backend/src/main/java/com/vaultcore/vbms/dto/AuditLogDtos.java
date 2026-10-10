package com.vaultcore.vbms.dto;

import java.time.LocalDateTime;

public final class AuditLogDtos {
    private AuditLogDtos() { }

    public record AuditLogResponse(
            Integer logId,
            Integer transactionId,
            Integer performedBy,
            String action,
            LocalDateTime logTime,
            String remarks) { }
}
