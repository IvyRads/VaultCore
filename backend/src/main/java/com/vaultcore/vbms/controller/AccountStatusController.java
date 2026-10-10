package com.vaultcore.vbms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.dto.CustomerManagementDtos.AccountResponse;
import com.vaultcore.vbms.dto.AccountDtos.AccountCreateRequest;
import com.vaultcore.vbms.dto.AccountDtos.AccountLookupResponse;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.AccountService;
import com.vaultcore.vbms.service.AccountStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/accounts")
public class AccountStatusController {
    private final AccountStatusService service;
    private final AuthorizationService authorization;
    private final AccountService accountService;

    public AccountStatusController(AccountStatusService service, AuthorizationService authorization,
            AccountService accountService) {
        this.service = service;
        this.authorization = authorization;
        this.accountService = accountService;
    }

    public record AccountActionRequest(@NotBlank String reason, String remarks) {}

    @GetMapping
    public List<AccountResponse> list(Authentication authentication) {
        return responses(service.list(authorization.caller(authentication)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<AccountResponse> mine(Authentication authentication) {
        return responses(service.list(authorization.caller(authentication)));
    }

    private List<AccountResponse> responses(List<Account> accounts) {
        return accounts.stream()
                .map(account -> new AccountResponse(account.getAccountId(), account.getAccountNumber(),
                        account.getAccountType(), account.getBalance(), account.getStatus(), account.getCreatedAt()))
                .toList();
    }

    @PostMapping
    public AccountResponse create(@Valid @RequestBody AccountCreateRequest request, Authentication authentication) {
        var caller = authorization.caller(authentication);
        return accountService.createForCustomer(caller.requireCustomerId(), request, caller.userId());
    }

    @GetMapping("/lookup/{number}")
    public AccountLookupResponse lookup(@PathVariable String number) {
        return accountService.lookup(number);
    }

    @PostMapping("/{id:\\d+}/freeze")
    public Account freeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "FREEZE", request.reason(), request.remarks(), authorization.caller(authentication));
    }

    @PostMapping("/{id:\\d+}/unfreeze")
    public Account unfreeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "UNFREEZE", request.reason(), request.remarks(), authorization.caller(authentication));
    }
}
