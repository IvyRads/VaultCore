package com.vaultcore.vbms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.AccountStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/accounts")
public class AccountStatusController {
    private final AccountStatusService service;
    private final AuthorizationService authorization;

    public AccountStatusController(AccountStatusService service, AuthorizationService authorization) {
        this.service = service;
        this.authorization = authorization;
    }

    public record AccountActionRequest(@NotBlank String reason, String remarks) {}

    @GetMapping
    public List<Account> list(Authentication authentication) {
        return service.list(authorization.caller(authentication));
    }

    @PostMapping("/{id}/freeze")
    public Account freeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "FREEZE", request.reason(), request.remarks(), authorization.caller(authentication));
    }

    @PostMapping("/{id}/unfreeze")
    public Account unfreeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "UNFREEZE", request.reason(), request.remarks(), authorization.caller(authentication));
    }
}
