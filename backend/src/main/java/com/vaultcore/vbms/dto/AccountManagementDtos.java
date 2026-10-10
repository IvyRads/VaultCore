package com.vaultcore.vbms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class AccountManagementDtos {
    private AccountManagementDtos() { }

    public record AdminAccountResponse(
            Integer accountId,
            String accountNumber,
            String accountType,
            BigDecimal balance,
            String status,
            LocalDateTime createdAt,
            Integer customerId,
            String customerNumber,
            String customerName,
            Integer branchId,
            String branchName) { }
}
