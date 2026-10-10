package com.vaultcore.vbms.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.CustomerManagementDtos.AccountResponse;
import com.vaultcore.vbms.dto.CustomerManagementDtos.CustomerResponse;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;

@Service
public class CustomerManagementService {
    private final CustomerRepository customers;
    private final AccountRepository accounts;
    private final UserProfileRepository profiles;

    public CustomerManagementService(CustomerRepository customers, AccountRepository accounts,
            UserProfileRepository profiles) {
        this.customers = customers;
        this.accounts = accounts;
        this.profiles = profiles;
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> listCustomers(String search, Integer branchId) {
        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();
        return customers.searchCustomers(normalizedSearch, branchId).stream()
                .map(this::customerResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomer(Integer customerId) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFound("Customer not found"));
        return customerResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getCustomerAccounts(Integer customerId) {
        if (!customers.existsById(customerId)) {
            throw new NotFound("Customer not found");
        }
        return accounts.findByCustomerCustomerId(customerId).stream()
                .map(account -> new AccountResponse(
                        account.getAccountId(),
                        account.getAccountNumber(),
                        account.getAccountType(),
                        account.getBalance(),
                        account.getStatus(),
                        account.getCreatedAt()))
                .toList();
    }

    private CustomerResponse customerResponse(Customer customer) {
        UserProfile profile = profiles.findByUserUserId(customer.getUser().getUserId()).orElse(null);
        var branch = customer.getBranch();
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getCustomerNumber(),
                profile == null ? null : profile.getFirstName(),
                profile == null ? null : profile.getLastName(),
                profile == null ? null : profile.getPhone(),
                profile == null ? null : profile.getDob(),
                profile == null ? null : profile.getGender(),
                profile == null ? null : profile.getAddress(),
                branch == null ? null : branch.getBranchId(),
                branch == null ? null : branch.getBranchName(),
                customer.getCreatedAt());
    }
}
