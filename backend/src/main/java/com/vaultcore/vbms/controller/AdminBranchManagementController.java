package com.vaultcore.vbms.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.BranchManagementDtos.AccountResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.BranchResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.BranchStatsResponse;
import com.vaultcore.vbms.dto.BranchManagementDtos.CustomerResponse;
import com.vaultcore.vbms.service.BranchManagementService;

@RestController
@RequestMapping("/api/admin/branches")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBranchManagementController {
    private final BranchManagementService branchManagement;

    public AdminBranchManagementController(BranchManagementService branchManagement) {
        this.branchManagement = branchManagement;
    }

    @GetMapping
    public List<BranchResponse> searchBranches(
            @RequestParam(required = false) String branchName,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String ifscCode) {
        return branchManagement.searchBranches(branchName, district, ifscCode);
    }

    @GetMapping("/{id}/customers")
    public List<CustomerResponse> customers(@PathVariable Integer id) {
        return branchManagement.customers(id);
    }

    @GetMapping("/{id}/accounts")
    public List<AccountResponse> accounts(@PathVariable Integer id) {
        return branchManagement.accounts(id);
    }

    @GetMapping("/{id}/stats")
    public BranchStatsResponse stats(@PathVariable Integer id) {
        return branchManagement.stats(id);
    }
}
