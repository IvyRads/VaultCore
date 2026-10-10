package com.vaultcore.vbms.dto;

public final class AdminSummaryDtos {
    private AdminSummaryDtos() { }

    public record AdminSummaryResponse(
            long totalCustomers,
            long totalBranches,
            long totalAccounts,
            long activeAccounts,
            long totalTransactions,
            String bankStatus,
            Integer adminUserId,
            String adminEmail,
            String adminName,
            String lastLogin) { }
}
