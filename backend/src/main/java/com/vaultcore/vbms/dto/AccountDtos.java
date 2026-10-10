package com.vaultcore.vbms.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public final class AccountDtos {
    private AccountDtos() { }

    public record AccountCreateRequest(@NotBlank String accountType, BigDecimal initialDeposit) { }

    public record AccountLookupResponse(Integer accountId, String maskedAccountNumber, String ownerDisplayName) { }
}
