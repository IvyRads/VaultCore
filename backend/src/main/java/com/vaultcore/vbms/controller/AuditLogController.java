package com.vaultcore.vbms.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.AuditLogDtos.AuditLogResponse;
import com.vaultcore.vbms.service.AuditLogService;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {
    private final AuditLogService auditLogs;

    public AuditLogController(AuditLogService auditLogs) {
        this.auditLogs = auditLogs;
    }

    @GetMapping
    public List<AuditLogResponse> list(
            @RequestParam(required = false) Integer performedBy,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return auditLogs.list(performedBy, action, from, to);
    }

    @GetMapping("/{id}")
    public AuditLogResponse get(@PathVariable Integer id) {
        return auditLogs.get(id);
    }
}
