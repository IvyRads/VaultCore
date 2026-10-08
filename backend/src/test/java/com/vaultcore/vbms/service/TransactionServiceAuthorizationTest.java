package com.vaultcore.vbms.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultcore.vbms.dto.TransactionDtos.MoneyRequest;
import com.vaultcore.vbms.dto.TransactionDtos.TransferRequest;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.exception.ApiExceptions.Forbidden;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.TransactionLogRepository;
import com.vaultcore.vbms.repository.TransactionRepository;
import com.vaultcore.vbms.repository.TransferRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@ExtendWith(MockitoExtension.class)
class TransactionServiceAuthorizationTest {
    @Mock private AccountRepository accounts;
    @Mock private TransactionRepository transactions;
    @Mock private TransferRepository transfers;
    @Mock private TransactionLogRepository logs;
    @Mock private UserRepository users;
    @Mock private CustomerRepository customers;

    @Test
    void depositRejectsAnAccountOwnedByAnotherCustomerBeforeChangingBalance() {
        Account account = account(42, 200, "100.00");
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account));

        TransactionService service = service();

        assertThrows(Forbidden.class, () -> service.deposit(
                42,
                new MoneyRequest(42, new BigDecimal("25.00"), "deposit"),
                new Caller(10, 100, false)));

        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), account.getBalance());
        verify(accounts, never()).save(account);
        verifyNoInteractions(transactions, logs, transfers);
    }

    @Test
    void transferRejectsARequestFromAnAccountOwnedByAnotherCustomer() {
        Account sender = account(42, 200, "100.00");
        Account receiver = account(99, 100, "50.00");
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(sender));
        when(accounts.findForUpdate(99)).thenReturn(Optional.of(receiver));

        TransactionService service = service();

        assertThrows(Forbidden.class, () -> service.transfer(
                new TransferRequest(42, 99, new BigDecimal("25.00")),
                new Caller(10, 100, false)));

        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("100.00"), sender.getBalance());
        org.junit.jupiter.api.Assertions.assertEquals(new BigDecimal("50.00"), receiver.getBalance());
        verifyNoInteractions(transactions, logs, transfers);
    }

    private TransactionService service() {
        AuthorizationService authorization = new AuthorizationService(users, customers);
        return new TransactionService(accounts, transactions, transfers, logs, authorization);
    }

    private Account account(int accountId, int customerId, String balance) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCustomer(customer);
        account.setBalance(new BigDecimal(balance));
        account.setStatus("ACTIVE");
        return account;
    }
}
