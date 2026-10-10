package com.vaultcore.vbms.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.AdminSummaryDtos.AdminSummaryResponse;
import com.vaultcore.vbms.service.AdminSummaryService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSummaryController {
    private final AdminSummaryService summary;

    public AdminSummaryController(AdminSummaryService summary) {
        this.summary = summary;
    }

    @GetMapping("/summary")
    public AdminSummaryResponse summary(Authentication authentication) {
        return summary.summary(authentication.getName());
    }
}
