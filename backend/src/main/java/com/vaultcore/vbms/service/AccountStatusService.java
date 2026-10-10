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
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@Service
public class AccountStatusService {
    private final AccountRepository accounts;
    private final AccountRequestRepository requests;
    private final AuthorizationService authorization;

    public AccountStatusService(AccountRepository accounts, AccountRequestRepository requests, AuthorizationService authorization) {
        this.accounts = accounts;
        this.requests = requests;
        this.authorization = authorization;
    }

    public List<Account> list(Caller caller) {
        if (caller.admin()) return accounts.findAll();
        return caller.customerId() == null ? List.of() : accounts.findByCustomerCustomerId(caller.customerId());
    }

    @Transactional
    public Account changeStatus(Integer accountId, String action, String reason, String remarks, Caller caller) {
        Account account = accounts.findForUpdate(accountId).orElseThrow(() -> new NotFound("Account not found"));
        authorization.requireAccountAccess(account, caller);
        if ("CLOSED".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequest("Closed accounts cannot be frozen or unfrozen");
        }
        String targetStatus;
        if ("FREEZE".equalsIgnoreCase(action) && "ACTIVE".equalsIgnoreCase(account.getStatus())) {
            targetStatus = "FROZEN";
        } else if ("UNFREEZE".equalsIgnoreCase(action) && "FROZEN".equalsIgnoreCase(account.getStatus())) {
            targetStatus = "ACTIVE";
        } else if ("FREEZE".equalsIgnoreCase(action)) {
            throw new BadRequest("Only active accounts can be frozen");
        } else if ("UNFREEZE".equalsIgnoreCase(action)) {
            throw new BadRequest("Only frozen accounts can be unfrozen");
        }
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
