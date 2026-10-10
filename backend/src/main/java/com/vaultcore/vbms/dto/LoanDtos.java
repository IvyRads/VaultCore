package com.vaultcore.vbms.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public final class LoanDtos {
    private LoanDtos() { }

    public record LoanApplicationRequest(
            @NotBlank String loanType,
            @NotNull @Positive BigDecimal principal,
            @NotNull @Positive Integer tenureMonths) { }

    public record LoanResponse(
            Integer loanId,
            String loanType,
            BigDecimal principal,
            BigDecimal interestRate,
            Integer tenureMonths,
            BigDecimal emi,
            BigDecimal outstanding,
            String status,
            LocalDateTime appliedAt,
            LocalDateTime approvedAt) { }
}
