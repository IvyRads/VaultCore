package com.vaultcore.vbms.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vaultcore.vbms.entity.FraudReport;

public interface FraudReportRepository extends JpaRepository<FraudReport, Integer> {
    List<FraudReport> findByCustomerCustomerIdOrderByCreatedAtDesc(Integer customerId);
}
