package com.vaultcore.vbms.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountTypeRepository;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.service.CustomerManagementService;
import com.vaultcore.vbms.dto.CustomerManagementDtos.AccountResponse;
import com.vaultcore.vbms.dto.CustomerManagementDtos.CustomerResponse;
import com.vaultcore.vbms.dto.AccountManagementDtos.AdminAccountResponse;
import com.vaultcore.vbms.service.AccountManagementService;
import java.util.List;

@RestController @RequestMapping("/api")
public class ReferenceDataController {
    private final AccountRepository accounts; private final BranchRepository branches; private final AccountTypeRepository types;
    private final CustomerManagementService customerManagement;
    private final AccountManagementService accountManagement;
    public ReferenceDataController(AccountRepository a,BranchRepository b,AccountTypeRepository t,
            CustomerManagementService customerManagement, AccountManagementService accountManagement){
        accounts=a;branches=b;types=t;this.customerManagement=customerManagement;this.accountManagement=accountManagement;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers") public List<CustomerResponse> customers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer branchId) {
        return customerManagement.listCustomers(search, branchId);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/{id}") public CustomerResponse customer(@PathVariable int id){return customerManagement.getCustomer(id);}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/customers/{id}/accounts") public List<AccountResponse> customerAccounts(@PathVariable int id){
        return customerManagement.getCustomerAccounts(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reference/accounts") public List<AdminAccountResponse> accounts(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer branchId) {
        return accountManagement.list(accountNumber, accountType, status, customerId, branchId);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accounts/{id:\\d+}") public AdminAccountResponse account(@PathVariable int id){
        return accountManagement.get(id);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accounts/number/{number}") public Object accountByNumber(@PathVariable String number){return accounts.findByAccountNumber(number).orElseThrow();}
    @GetMapping("/branches") public Object branches(){return branches.findAll();}
    @GetMapping("/branches/{id}") public Object branch(@PathVariable int id){return branches.findById(id).orElseThrow();}
    @GetMapping("/account-types") public Object accountTypes(){return types.findAll();}
}
