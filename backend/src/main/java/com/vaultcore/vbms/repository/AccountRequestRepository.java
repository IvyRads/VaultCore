package com.vaultcore.vbms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultcore.vbms.entity.AccountRequest;

public interface AccountRequestRepository extends JpaRepository<AccountRequest, Integer> {
    List<AccountRequest> findByAccountCustomerCustomerIdOrderByRequestDateDesc(Integer customerId);
}
