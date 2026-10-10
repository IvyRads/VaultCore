package com.vaultcore.vbms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.AdminSummaryDtos.AdminSummaryResponse;
import com.vaultcore.vbms.entity.User;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.TransactionRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;
import com.vaultcore.vbms.repository.UserRepository;

@Service
public class AdminSummaryService {
    private final CustomerRepository customers;
    private final BranchRepository branches;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final UserRepository users;
    private final UserProfileRepository profiles;

    public AdminSummaryService(CustomerRepository customers, BranchRepository branches, AccountRepository accounts,
            TransactionRepository transactions, UserRepository users, UserProfileRepository profiles) {
        this.customers = customers;
        this.branches = branches;
        this.accounts = accounts;
        this.transactions = transactions;
        this.users = users;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public AdminSummaryResponse summary(String adminEmail) {
        User admin = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new NotFound("Admin user not found"));
        UserProfile profile = profiles.findByUserUserId(admin.getUserId()).orElse(null);
        String name = profile == null ? null : ((profile.getFirstName() == null ? "" : profile.getFirstName())
                + " " + (profile.getLastName() == null ? "" : profile.getLastName())).trim();
        if (name == null || name.isBlank()) name = admin.getEmail();

        return new AdminSummaryResponse(
                customers.count(),
                branches.count(),
                accounts.count(),
                accounts.countByStatusIgnoreCase("ACTIVE"),
                transactions.count(),
                "Operational",
                admin.getUserId(),
                admin.getEmail(),
                name,
                admin.getLastLogin() == null ? "First login" : admin.getLastLogin().toString());
    }
}
