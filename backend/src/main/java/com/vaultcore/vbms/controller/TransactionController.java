package com.vaultcore.vbms.controller;
import com.vaultcore.vbms.dto.TransactionDtos.*;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.TransactionService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
 @GetMapping("/accounts/{id:\\d+}/transactions") public Object accountTransactions(@PathVariable Integer id,Authentication authentication){return service.accountTransactions(id,authorization.caller(authentication));}
 @GetMapping("/customers/{id}/transactions") public Object customerTransactions(@PathVariable Integer id,Authentication authentication){return service.customerTransactions(id,authorization.caller(authentication));}
 @PreAuthorize("hasRole('CUSTOMER')")
 @GetMapping("/transactions/mine") public Object myTransactions(Authentication authentication){return service.myTransactions(authorization.caller(authentication));}
 @GetMapping("/accounts/{id:\\d+}/transfers") public Object accountTransfers(@PathVariable Integer id,Authentication authentication){return service.accountTransfers(id,authorization.caller(authentication));}
 @PreAuthorize("hasRole('ADMIN')")
 @GetMapping("/admin/transactions") public List<TransactionMonitoringResponse> monitorTransactions(
         @RequestParam(required=false) String transactionType,
         @RequestParam(required=false) Integer accountId,
         @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
         @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
     return service.monitorTransactions(transactionType, accountId, from, to);
 }
 @PreAuthorize("hasRole('ADMIN')")
 @GetMapping("/admin/transactions/{id}") public TransactionMonitoringResponse monitorTransaction(@PathVariable Integer id) {
     return service.monitorTransaction(id);
 }
}
