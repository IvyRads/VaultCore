package com.vaultcore.vbms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.ProfileDtos.ProfileResponse;
import com.vaultcore.vbms.dto.ProfileDtos.ProfileUpdateRequest;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@Service
public class ProfileService {
    private final UserProfileRepository profiles;
    private final CustomerRepository customers;

    public ProfileService(UserProfileRepository profiles, CustomerRepository customers) {
        this.profiles = profiles;
        this.customers = customers;
    }

    @Transactional(readOnly = true)
    public ProfileResponse get(Caller caller) {
        UserProfile profile = profiles.findByUserUserId(caller.userId())
                .orElseThrow(() -> new NotFound("Customer profile not found"));
        return response(profile);
    }

    @Transactional
    public ProfileResponse update(ProfileUpdateRequest request, Caller caller) {
        UserProfile profile = profiles.findByUserUserId(caller.userId())
                .orElseThrow(() -> new NotFound("Customer profile not found"));
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setDob(request.dob());
        profile.setGender(request.gender());
        profile.setAddress(request.address());
        return response(profile);
    }

    private ProfileResponse response(UserProfile profile) {
        var customer = customers.findByUserUserId(profile.getUser().getUserId())
                .orElseThrow(() -> new NotFound("Customer profile not found"));
        return new ProfileResponse(profile.getFirstName(), profile.getLastName(), profile.getPhone(),
                profile.getDob(), profile.getGender(), profile.getAddress(), profile.getUser().getEmail(),
                customer.getCustomerNumber(), customer.getBranch().getBranchName(), customer.getCreatedAt(),
                profile.getUser().getLastLogin());
    }
}
