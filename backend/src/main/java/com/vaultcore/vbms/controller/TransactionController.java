package com.vaultcore.vbms.controller;
import com.vaultcore.vbms.dto.TransactionDtos.*;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class TransactionController {
 private final TransactionService service;
 private final AuthorizationService authorization;
 public TransactionController(TransactionService service, AuthorizationService authorization){this.service=service;this.authorization=authorization;}
 @PostMapping("/transactions/deposit") public Object deposit(@Valid @RequestBody MoneyRequest request,Authentication authentication){return service.deposit(request.accountId(),request,authorization.caller(authentication));}
 @PostMapping("/transactions/withdraw") public Object withdraw(@Valid @RequestBody MoneyRequest request,Authentication authentication){return service.withdraw(request.accountId(),request,authorization.caller(authentication));}
 @PostMapping("/transfers") public Object transfer(@Valid @RequestBody TransferRequest request,Authentication authentication){return service.transfer(request,authorization.caller(authentication));}
 @GetMapping("/accounts/{id}/transactions") public Object accountTransactions(@PathVariable Integer id,Authentication authentication){return service.accountTransactions(id,authorization.caller(authentication));}
 @GetMapping("/customers/{id}/transactions") public Object customerTransactions(@PathVariable Integer id,Authentication authentication){return service.customerTransactions(id,authorization.caller(authentication));}
 @GetMapping("/accounts/{id}/transfers") public Object accountTransfers(@PathVariable Integer id,Authentication authentication){return service.accountTransfers(id,authorization.caller(authentication));}
}
