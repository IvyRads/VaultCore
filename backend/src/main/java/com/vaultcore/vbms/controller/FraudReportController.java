package com.vaultcore.vbms.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.entity.FraudReport;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.FraudReportService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/fraud-reports")
public class FraudReportController {
    private final FraudReportService service;
    private final AuthorizationService authorization;

    public FraudReportController(FraudReportService service, AuthorizationService authorization) {
        this.service = service;
        this.authorization = authorization;
    }

    public record FraudReportRequest(@NotNull Integer accountId, Integer transactionId, @NotBlank String fraudType, @NotBlank String description, @NotNull LocalDate incidentDate, @NotBlank String contactPreference) {}

    @GetMapping
    public List<FraudReport> list(Authentication authentication) {
        return service.list(authorization.caller(authentication));
    }

    @PostMapping
    public FraudReport create(@Valid @RequestBody FraudReportRequest request, Authentication authentication) {
        return service.create(authorization.caller(authentication), request.accountId(), request.transactionId(), request.fraudType(), request.description(), request.incidentDate(), request.contactPreference());
    }
}
