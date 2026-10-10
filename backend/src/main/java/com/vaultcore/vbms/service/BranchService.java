package com.vaultcore.vbms.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.BranchDtos.BranchResponse;
import com.vaultcore.vbms.dto.BranchDtos.BranchUpdateRequest;
import com.vaultcore.vbms.entity.Branch;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.BranchRepository;

@Service
public class BranchService {
    private final BranchRepository branches;

    public BranchService(BranchRepository branches) {
        this.branches = branches;
    }

    @Transactional
    public BranchResponse update(Integer branchId, BranchUpdateRequest request) {
        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new NotFound("Branch not found"));
        branch.setBranchName(request.branchName());
        branch.setDistrict(request.district());
        branch.setAddress(request.address());
        branch.setPhone(request.phone());
        return response(branch);
    }

    private BranchResponse response(Branch branch) {
        return new BranchResponse(branch.getBranchId(), branch.getBranchName(), branch.getDistrict(),
                branch.getAddress(), branch.getPhone());
    }
}
