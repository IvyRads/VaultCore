package com.vaultcore.vbms.service;

import com.vaultcore.vbms.entity.*;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudReportService {
    private final FraudReportRepository reports;
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final CustomerRepository customers;

    public FraudReportService(FraudReportRepository reports, AccountRepository accounts, TransactionRepository transactions, CustomerRepository customers) {
        this.reports = reports;
        this.accounts = accounts;
        this.transactions = transactions;
        this.customers = customers;
    }

    public List<FraudReport> list(Integer customerId) {
        return reports.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional
    public FraudReport create(Integer customerId, Integer accountId, Integer transactionId, String fraudType, String description, java.time.LocalDate incidentDate, String contactPreference) {
        Customer customer = customers.findById(customerId).orElseThrow(() -> new NotFound("Customer not found"));
        Account account = accounts.findById(accountId).orElseThrow(() -> new NotFound("Account not found"));
        if (!account.getCustomer().getCustomerId().equals(customerId)) throw new NotFound("Account not found");
        Transaction transaction = transactionId == null ? null : transactions.findById(transactionId).orElseThrow(() -> new NotFound("Transaction not found"));
        if (transaction != null && !transaction.getAccount().getAccountId().equals(accountId)) throw new NotFound("Transaction not found");
        FraudReport report = new FraudReport();
        report.setCustomer(customer);
        report.setAccount(account);
        report.setTransaction(transaction);
        report.setFraudType(fraudType);
        report.setDescription(description);
        report.setIncidentDate(incidentDate);
        report.setContactPreference(contactPreference);
        report.setStatus("SUBMITTED");
        report.setCreatedAt(LocalDateTime.now());
        return reports.save(report);
    }
}
