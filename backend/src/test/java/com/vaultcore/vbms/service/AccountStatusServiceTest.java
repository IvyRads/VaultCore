package com.vaultcore.vbms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.AccountRequest;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountRequestRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@ExtendWith(MockitoExtension.class)
class AccountStatusServiceTest {
    @Mock private AccountRepository accounts;
    @Mock private AccountRequestRepository requests;
    @Mock private UserRepository users;
    @Mock private CustomerRepository customers;

    @Test
    void freezeActiveAccountSavesStatusAndRequest() {
        Account account = account("ACTIVE");
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account));
        when(accounts.save(account)).thenReturn(account);

        Account result = service().changeStatus(42, "FREEZE", "requested", null, admin());

        assertEquals("FROZEN", result.getStatus());
        verify(accounts).save(account);
        verify(requests).save(any(AccountRequest.class));
    }

    @Test
    void unfreezeFrozenAccountSavesStatusAndRequest() {
        Account account = account("FROZEN");
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account));
        when(accounts.save(account)).thenReturn(account);

        Account result = service().changeStatus(42, "UNFREEZE", "requested", null, admin());

        assertEquals("ACTIVE", result.getStatus());
        verify(accounts).save(account);
        verify(requests).save(any(AccountRequest.class));
    }

    @Test
    void myAccountsUsesTheCallersCustomerIdRatherThanUserId() {
        Account ownAccount = account("ACTIVE");
        ownAccount.getCustomer().setCustomerId(25);
        when(accounts.findByCustomerCustomerId(25)).thenReturn(List.of(ownAccount));

        List<Account> result = service().list(new Caller(3, 25, false));

        assertEquals(1, result.size());
        assertSame(ownAccount, result.get(0));
        verify(accounts).findByCustomerCustomerId(25);
        verify(accounts, never()).findAll();
    }

    @ParameterizedTest
    @MethodSource("rejectedTransitions")
    void rejectedTransitionsDoNotSaveAccountOrRequest(String action, String status, String expectedMessage) {
        Account account = account(status);
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account));

        BadRequest error = assertThrows(BadRequest.class,
                () -> service().changeStatus(42, action, "requested", null, admin()));

        assertEquals(expectedMessage, error.getMessage());
        assertEquals(status, account.getStatus());
        verify(accounts, never()).save(any(Account.class));
        verify(requests, never()).save(any(AccountRequest.class));
    }

    static Stream<Arguments> rejectedTransitions() {
        return Stream.of(
                Arguments.of("FREEZE", "FROZEN", "Only active accounts can be frozen"),
                Arguments.of("FREEZE", "CLOSED", "Closed accounts cannot be frozen or unfrozen"),
                Arguments.of("UNFREEZE", "ACTIVE", "Only frozen accounts can be unfrozen"),
                Arguments.of("UNFREEZE", "CLOSED", "Closed accounts cannot be frozen or unfrozen"),
                Arguments.of("UNFREEZE", null, "Only frozen accounts can be unfrozen"),
                Arguments.of("UNFREEZE", "UNKNOWN", "Only frozen accounts can be unfrozen"),
                Arguments.of("FREEZE", null, "Only active accounts can be frozen"),
                Arguments.of("OTHER", "CLOSED", "Closed accounts cannot be frozen or unfrozen"),
                Arguments.of("OTHER", "ACTIVE", "Action must be FREEZE or UNFREEZE"),
                Arguments.of(null, "ACTIVE", "Action must be FREEZE or UNFREEZE"));
    }

    private AccountStatusService service() {
        AuthorizationService authorization = new AuthorizationService(users, customers);
        return new AccountStatusService(accounts, requests, authorization);
    }

    private Caller admin() {
        return new Caller(1, null, true);
    }

    private Account account(String status) {
        Account account = new Account();
        account.setAccountId(42);
        Customer customer = new Customer();
        customer.setCustomerId(10);
        account.setCustomer(customer);
        account.setStatus(status);
        return account;
    }
}
