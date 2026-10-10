package com.vaultcore.vbms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class CustomerManagementDtos {
    private CustomerManagementDtos() { }

    public record CustomerResponse(
            Integer customerId,
            String customerNumber,
            String firstName,
            String lastName,
            String phone,
            LocalDate dob,
            String gender,
            String address,
            Integer branchId,
            String branchName,
            LocalDateTime createdAt) { }

    public record AccountResponse(
            Integer accountId,
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String status,
            LocalDateTime createdAt) { }
}
