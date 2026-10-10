package com.vaultcore.vbms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.BranchManagementDtos.AccountResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.BranchResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.BranchStatsResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.CustomerResponse;
import com.vaultcore.vbms.entity.Branch;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;

@Service
public class BranchManagementService {
    private final BranchRepository branches;
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final UserProfileRepository profiles;

    public BranchManagementService(BranchRepository branches, CustomerRepository customers,
            AccountRepository accounts, UserProfileRepository profiles) {
        this.branches = branches;
        this.customers = customers;
        this.accounts = accounts;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> searchBranches(String branchName, String district, String ifscCode) {
        return branches.searchBranches(normalize(branchName), normalize(district), normalize(ifscCode)).stream()
                .map(this::branchResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> customers(Integer branchId) {
        requireBranch(branchId);
        return customers.findByBranchId(branchId).stream()
                .map(this::customerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> accounts(Integer branchId) {
        requireBranch(branchId);
        return accounts.findByCustomerBranchId(branchId).stream()
                .map(account -> new AccountResponse(
                        account.getAccountId(),
                        account.getAccountNumber(),
                        account.getAccountType(),
                        account.getBalance(),
                        account.getStatus(),
                        account.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public BranchStatsResponse stats(Integer branchId) {
        requireBranch(branchId);
        return new BranchStatsResponse(
                customers.countByBranchId(branchId),
                accounts.countByCustomerBranchId(branchId),
                accounts.sumBalanceByCustomerBranchId(branchId));
    }

    private void requireBranch(Integer branchId) {
        if (!branches.existsById(branchId)) {
            throw new NotFound("Branch not found");
        }
    }

    private BranchResponse branchResponse(Branch branch) {
        return new BranchResponse(branch.getBranchId(), branch.getBranchName(), branch.getDistrict(),
                branch.getAddress(), branch.getIfscCode(), branch.getPhone());
    }

    private CustomerResponse customerResponse(Customer customer) {
        UserProfile profile = profiles.findByUserUserId(customer.getUser().getUserId()).orElse(null);
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getCustomerNumber(),
                profile == null ? null : profile.getFirstName(),
                profile == null ? null : profile.getLastName(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getDob(),
                profile == null ? null : profile.getGender(),
                profile == null ? null : profile.getAddress());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
