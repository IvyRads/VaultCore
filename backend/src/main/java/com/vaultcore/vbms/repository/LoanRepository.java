package com.vaultcore.vbms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultcore.vbms.entity.Loan;

public interface LoanRepository extends JpaRepository<Loan, Integer> {
    List<Loan> findByCustomerCustomerIdOrderByAppliedAtDesc(Integer customerId);
}
