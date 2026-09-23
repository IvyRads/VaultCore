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
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.service.FraudReportService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/fraud-reports")
public class FraudReportController {
    private final FraudReportService service;
    private final UserRepository users;
    private final CustomerRepository customers;

    public FraudReportController(FraudReportService service, UserRepository users, CustomerRepository customers) {
        this.service = service;
        this.users = users;
        this.customers = customers;
    }

    public record FraudReportRequest(@NotNull Integer accountId, Integer transactionId, @NotBlank String fraudType, @NotBlank String description, @NotNull LocalDate incidentDate, @NotBlank String contactPreference) {}

    @GetMapping
    public List<FraudReport> list(Authentication authentication) {
        return service.list(customerId(authentication));
    }

    @PostMapping
    public FraudReport create(@Valid @RequestBody FraudReportRequest request, Authentication authentication) {
        return service.create(customerId(authentication), request.accountId(), request.transactionId(), request.fraudType(), request.description(), request.incidentDate(), request.contactPreference());
    }

    private Integer customerId(Authentication authentication) {
        var user = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        return customers.findByUserUserId(user.getUserId()).orElseThrow().getCustomerId();
    }
}
