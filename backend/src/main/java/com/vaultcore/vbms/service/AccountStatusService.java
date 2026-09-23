package com.vaultcore.vbms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.AccountRequest;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountRequestRepository;

@Service
public class AccountStatusService {
    private final AccountRepository accounts;
    private final AccountRequestRepository requests;

    public AccountStatusService(AccountRepository accounts, AccountRequestRepository requests) {
        this.accounts = accounts;
        this.requests = requests;
    }

    public List<Account> list(Integer customerId) {
        return accounts.findByCustomerCustomerId(customerId);
    }

    @Transactional
    public Account changeStatus(Integer accountId, String action, String reason, String remarks, Integer customerId) {
        Account account = accounts.findForUpdate(accountId).orElseThrow(() -> new NotFound("Account not found"));
        if (!account.getCustomer().getCustomerId().equals(customerId)) {
            throw new NotFound("Account not found");
        }
        String targetStatus;
        if ("FREEZE".equalsIgnoreCase(action)) targetStatus = "FROZEN";
        else if ("UNFREEZE".equalsIgnoreCase(action)) targetStatus = "ACTIVE";
        else throw new BadRequest("Action must be FREEZE or UNFREEZE");
        account.setStatus(targetStatus);
        Account saved = accounts.save(account);
        AccountRequest request = new AccountRequest();
        request.setAccount(account);
        request.setAction(action.toUpperCase());
        request.setReason(reason);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("COMPLETED");
        requests.save(request);
        return saved;
    }
}
