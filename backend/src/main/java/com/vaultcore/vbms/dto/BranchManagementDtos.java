package com.vaultcore.vbms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class BranchManagementDtos {
    private BranchManagementDtos() { }

    public record BranchResponse(
            Integer branchId,
            String branchName,
            String district,
            String address,
            String ifscCode,
            String phone) { }

    public record CustomerResponse(
            Integer customerId,
            String customerNumber,
            String firstName,
            String lastName,
            String phone,
            LocalDate dob,
            String gender,
            String address) { }

    public record AccountResponse(
            Integer accountId,
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String status,
            LocalDateTime createdAt) { }

    public record BranchStatsResponse(long customerCount, long accountCount, BigDecimal totalBalance) { }
}
