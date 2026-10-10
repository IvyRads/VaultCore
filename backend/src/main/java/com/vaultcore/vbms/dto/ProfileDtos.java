package com.vaultcore.vbms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public final class ProfileDtos {
    private ProfileDtos() { }

    public record ProfileUpdateRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String phone,
            @Past LocalDate dob,
            @NotBlank String gender,
            @NotBlank String address) { }

    public record ProfileResponse(
            String firstName,
            String lastName,
            String phone,
            LocalDate dob,
            String gender,
            String address,
            String email,
            String customerNumber,
            String branchName,
            LocalDateTime createdAt,
            LocalDateTime lastLogin) { }
}
