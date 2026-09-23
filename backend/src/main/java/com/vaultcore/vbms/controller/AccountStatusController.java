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
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.service.AccountStatusService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/accounts")
public class AccountStatusController {
    private final AccountStatusService service;
    private final UserRepository users;
    private final CustomerRepository customers;

    public AccountStatusController(AccountStatusService service, UserRepository users, CustomerRepository customers) {
        this.service = service;
        this.users = users;
        this.customers = customers;
    }

    public record AccountActionRequest(@NotBlank String reason, String remarks) {}

    @GetMapping
    public List<Account> list(Authentication authentication) {
        return service.list(customerId(authentication));
    }

    @PostMapping("/{id}/freeze")
    public Account freeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "FREEZE", request.reason(), request.remarks(), customerId(authentication));
    }

    @PostMapping("/{id}/unfreeze")
    public Account unfreeze(@PathVariable Integer id, @Valid @RequestBody AccountActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "UNFREEZE", request.reason(), request.remarks(), customerId(authentication));
    }

    private Integer customerId(Authentication authentication) {
        var user = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        return customers.findByUserUserId(user.getUserId()).orElseThrow().getCustomerId();
    }
}
