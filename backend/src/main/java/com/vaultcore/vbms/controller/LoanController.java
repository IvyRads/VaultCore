package com.vaultcore.vbms.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.LoanDtos.LoanApplicationRequest;
import com.vaultcore.vbms.dto.LoanDtos.LoanResponse;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.LoanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loans;
    private final AuthorizationService authorization;

    public LoanController(LoanService loans, AuthorizationService authorization) {
        this.loans = loans;
        this.authorization = authorization;
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<LoanResponse> mine(Authentication authentication) {
        return loans.mine(authorization.caller(authentication));
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public LoanResponse apply(@Valid @RequestBody LoanApplicationRequest request, Authentication authentication) {
        return loans.apply(request, authorization.caller(authentication));
    }
}
