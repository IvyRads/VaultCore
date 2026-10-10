package com.vaultcore.vbms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.AccountManagementDtos.AdminAccountResponse;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;

@Service
public class AccountManagementService {
    private final AccountRepository accounts;

    public AccountManagementService(AccountRepository accounts) {
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<AdminAccountResponse> list(String accountNumber, String accountType, String status,
            Integer customerId, Integer branchId) {
        return accounts.searchAdminAccounts(
                normalize(accountNumber),
                normalize(accountType),
                normalize(status),
                customerId,
                branchId,
                null);
    }

    @Transactional(readOnly = true)
    public AdminAccountResponse get(Integer accountId) {
        return accounts.searchAdminAccounts(null, null, null, null, null, accountId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFound("Account not found"));
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
