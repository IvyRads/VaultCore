package com.vaultcore.vbms.controller;
import com.vaultcore.vbms.dto.TransactionDtos.*;
import com.vaultcore.vbms.repository.*;
import com.vaultcore.vbms.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api")
public class TransactionController {
 private final TransactionService service; private final UserRepository users; private final TransactionRepository transactions; private final TransferRepository transfers;
 public TransactionController(TransactionService s,UserRepository u,TransactionRepository t,TransferRepository tr){service=s;users=u;transactions=t;transfers=tr;}
 private Integer actor(Authentication a){return users.findByEmailIgnoreCase(a.getName()).map(x->x.getUserId()).orElse(null);}
 @PostMapping("/transactions/deposit") public Object deposit(@Valid @RequestBody MoneyRequest r,Authentication a){return service.deposit(r.accountId(),r,actor(a));}
 @PostMapping("/transactions/withdraw") public Object withdraw(@Valid @RequestBody MoneyRequest r,Authentication a){return service.withdraw(r.accountId(),r,actor(a));}
 @PostMapping("/transfers") public Object transfer(@Valid @RequestBody TransferRequest r,Authentication a){return service.transfer(r,actor(a));}
 @GetMapping("/accounts/{id}/transactions") public Object accountTransactions(@PathVariable Integer id){return transactions.findByAccountAccountIdOrderByTransactionDateDesc(id);}
 @GetMapping("/customers/{id}/transactions") public Object customerTransactions(@PathVariable Integer id){return transactions.findByAccountCustomerCustomerIdOrderByTransactionDateDesc(id);}
 @GetMapping("/accounts/{id}/transfers") public Object accountTransfers(@PathVariable Integer id){return transfers.findBySenderAccountAccountIdOrReceiverAccountAccountIdOrderByTransferDateDesc(id,id);}
}
