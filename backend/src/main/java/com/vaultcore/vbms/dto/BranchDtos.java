package com.vaultcore.vbms.dto;

import jakarta.validation.constraints.NotBlank;

public final class BranchDtos {
    private BranchDtos() { }

    public record BranchUpdateRequest(
            @NotBlank String branchName,
            @NotBlank String district,
            @NotBlank String address,
            @NotBlank String phone) { }

    public record BranchResponse(
            Integer branchId,
            String branchName,
            String district,
            String address,
            String phone) { }
}
