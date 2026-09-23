package com.vaultcore.vbms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountTypeRepository;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.repository.CustomerRepository;

@RestController @RequestMapping("/api")
public class ReferenceDataController {
    private final CustomerRepository customers; private final AccountRepository accounts; private final BranchRepository branches; private final AccountTypeRepository types;
    public ReferenceDataController(CustomerRepository c,AccountRepository a,BranchRepository b,AccountTypeRepository t){customers=c;accounts=a;branches=b;types=t;}
    @GetMapping("/customers") public Object customers(){return customers.findAll();}
    @GetMapping("/customers/{id}") public Object customer(@PathVariable Integer id){return customers.findById(id).orElseThrow();}
    @GetMapping("/accounts") public Object accounts(){return accounts.findAll();}
    @GetMapping("/accounts/{id}") public Object account(@PathVariable Integer id){return accounts.findById(id).orElseThrow();}
    @GetMapping("/accounts/number/{number}") public Object accountByNumber(@PathVariable String number){return accounts.findByAccountNumber(number).orElseThrow();}
    @GetMapping("/branches") public Object branches(){return branches.findAll();}
    @GetMapping("/branches/{id}") public Object branch(@PathVariable Integer id){return branches.findById(id).orElseThrow();}
    @GetMapping("/account-types") public Object accountTypes(){return types.findAll();}
}
