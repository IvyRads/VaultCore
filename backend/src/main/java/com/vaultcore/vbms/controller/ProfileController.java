package com.vaultcore.vbms.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.dto.ProfileDtos.ProfileResponse;
import com.vaultcore.vbms.dto.ProfileDtos.ProfileUpdateRequest;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers/me/profile")
public class ProfileController {
    private final ProfileService profiles;
    private final AuthorizationService authorization;

    public ProfileController(ProfileService profiles, AuthorizationService authorization) {
        this.profiles = profiles;
        this.authorization = authorization;
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ProfileResponse get(Authentication authentication) {
        return profiles.get(authorization.caller(authentication));
    }

    @PutMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ProfileResponse update(@Valid @RequestBody ProfileUpdateRequest request, Authentication authentication) {
        return profiles.update(request, authorization.caller(authentication));
    }
}
