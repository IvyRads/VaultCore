package com.vaultcore.vbms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.BranchDtos.BranchResponse;
import com.vaultcore.vbms.dto.BranchDtos.BranchUpdateRequest;
import com.vaultcore.vbms.service.BranchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/branches")
public class BranchController {
    private final BranchService branches;

    public BranchController(BranchService branches) {
        this.branches = branches;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public BranchResponse update(@PathVariable Integer id, @Valid @RequestBody BranchUpdateRequest request) {
        return branches.update(id, request);
    }
}
