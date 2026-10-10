package com.vaultcore.vbms.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.AccountDtos.AccountCreateRequest;
import com.vaultcore.vbms.dto.AccountDtos.AccountLookupResponse;
import com.vaultcore.vbms.dto.CustomerManagementDtos.AccountResponse;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.AccountType;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountTypeRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;

@Service
public class AccountService {
    private final AccountRepository accounts;
    private final AccountTypeRepository accountTypes;
    private final CustomerRepository customers;
    private final UserProfileRepository profiles;
    private final TransactionService transactions;

    public AccountService(AccountRepository accounts, AccountTypeRepository accountTypes, CustomerRepository customers,
            UserProfileRepository profiles, TransactionService transactions) {
        this.accounts = accounts;
        this.accountTypes = accountTypes;
        this.customers = customers;
        this.profiles = profiles;
        this.transactions = transactions;
    }

    @Transactional
    public AccountResponse createForCustomer(Integer customerId, AccountCreateRequest request, Integer actor) {
        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFound("Customer not found"));
        BigDecimal deposit = request.initialDeposit() == null ? BigDecimal.ZERO : request.initialDeposit();
        return response(create(customer, request.accountType(), deposit, actor));
    }

    @Transactional
    public Account create(Customer customer, String requestedType, BigDecimal initialDeposit, Integer actor) {
        validateAccountTypeAndDeposit(requestedType, initialDeposit);
        Account account = new Account();
        account.setCustomer(customer);
        account.setAccountNumber(uniqueAccountNumber(customer.getCustomerId()));
        account.setAccountType(normalizedType(requestedType));
        account.setBalance(initialDeposit);
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        Account saved = accounts.save(account);
        if (initialDeposit.signum() > 0) {
            transactions.recordInitialDeposit(saved, initialDeposit, actor);
        }
        return saved;
    }

    public void validateAccountTypeAndDeposit(String requestedType, BigDecimal initialDeposit) {
        if (initialDeposit == null || initialDeposit.signum() < 0) {
            throw new BadRequest("Initial deposit must be at least 0");
        }
        if (requestedType == null || requestedType.isBlank()) {
            throw new BadRequest("Account type is required");
        }
        String normalizedType = normalizeType(requestedType);
        if (!normalizedType.equals("savings") && !normalizedType.equals("current")) {
            throw new BadRequest("Account type must be Savings or Current");
        }
        AccountType type = findType(requestedType);
        if (type != null && type.getMinimumBalance() != null
                && initialDeposit.compareTo(type.getMinimumBalance()) < 0) {
            throw new BadRequest("Initial deposit must be at least " + type.getMinimumBalance()
                    + " for " + type.getTypeName());
        }
    }

    @Transactional(readOnly = true)
    public AccountLookupResponse lookup(String accountNumber) {
        Account account = accounts.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFound("Account not found"));
        String number = account.getAccountNumber();
        String masked = "*".repeat(Math.max(0, number.length() - 4)) + number.substring(Math.max(0, number.length() - 4));
        String name = profiles.findByUserUserId(account.getCustomer().getUser().getUserId())
                .map(profile -> ((profile.getFirstName() == null ? "" : profile.getFirstName()) + " "
                        + (profile.getLastName() == null ? "" : profile.getLastName())).trim())
                .filter(value -> !value.isBlank())
                .orElse("VaultCore customer");
        return new AccountLookupResponse(account.getAccountId(), masked, name);
    }

    private String uniqueAccountNumber(Integer customerId) {
        for (long attempt = 0; attempt < 100; attempt++) {
            String candidate = String.format(Locale.ROOT, "%012d", customerId.longValue() + attempt);
            if (candidate.length() == 12 && !accounts.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }
        throw new BadRequest("Unable to generate a unique account number");
    }

    private AccountType findType(String requestedType) {
        String normalized = normalizeType(requestedType);
        return accountTypes.findAll().stream()
                .filter(type -> normalizeType(type.getTypeName()).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    private String normalizedType(String requestedType) {
        return normalizeType(requestedType).equals("savings") ? "SAVINGS" : "CURRENT";
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.toLowerCase().replace(" account", "").replaceAll("[^a-z]", "");
    }

    private AccountResponse response(Account account) {
        return new AccountResponse(account.getAccountId(), account.getAccountNumber(), account.getAccountType(),
                account.getBalance(), account.getStatus(), account.getCreatedAt());
    }
}
