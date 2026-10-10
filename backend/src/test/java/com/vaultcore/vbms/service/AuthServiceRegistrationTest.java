package com.vaultcore.vbms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vaultcore.vbms.dto.AuthDtos.RegisterRequest;
import com.vaultcore.vbms.entity.Branch;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.AccountType;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.entity.Transaction;
import com.vaultcore.vbms.entity.TransactionLog;
import com.vaultcore.vbms.entity.User;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.AccountTypeRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.TransactionLogRepository;
import com.vaultcore.vbms.repository.TransactionRepository;
import com.vaultcore.vbms.repository.TransferRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceRegistrationTest {
    @Mock private UserRepository users;
    @Mock private UserProfileRepository profiles;
    @Mock private CustomerRepository customers;
    @Mock private BranchRepository branches;
    @Mock private BCryptPasswordEncoder encoder;
    @Mock private JwtService jwt;
    @Mock private AccountRepository accounts;
    @Mock private AccountTypeRepository accountTypes;
    @Mock private TransactionRepository transactions;
    @Mock private TransactionLogRepository logs;
    @Mock private TransferRepository transfers;
    @Mock private AuthorizationService authorization;

    @Test
    void registrationSavesCustomerWithConfiguredDefaultBranch() {
        Branch branch = new Branch();
        branch.setBranchId(23);
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(branches.findById(23)).thenReturn(Optional.of(branch));
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(42);
            return user;
        });
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(84);
            return customer;
        });
        when(accountTypes.findAll()).thenReturn(List.of());
        when(accounts.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(encoder.encode("Password123")).thenReturn("encoded-password");

        service(23).register(request());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customers).save(customerCaptor.capture());
        assertNotNull(customerCaptor.getValue().getBranch());
        assertEquals(branch, customerCaptor.getValue().getBranch());
        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profiles).save(profileCaptor.capture());
        assertEquals("Vault", profileCaptor.getValue().getFirstName());
        assertEquals("Customer", profileCaptor.getValue().getLastName());
        assertEquals("5551234567", profileCaptor.getValue().getPhone());
        assertEquals(LocalDate.of(1990, 1, 1), profileCaptor.getValue().getDob());
        assertEquals("UNSPECIFIED", profileCaptor.getValue().getGender());
        assertEquals("1 Main Street", profileCaptor.getValue().getAddress());
        verify(encoder).encode("Password123");
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(accountCaptor.capture());
        assertEquals(0, accountCaptor.getValue().getBalance().compareTo(BigDecimal.ZERO));
        assertEquals("ACTIVE", accountCaptor.getValue().getStatus());
        assertEquals(12, accountCaptor.getValue().getAccountNumber().length());
        verify(transactions, never()).save(any(Transaction.class));
    }

    @Test
    void registrationCreatesAccountAndRecordsInitialDeposit() {
        prepareRegistration();

        service(23).register(request(new BigDecimal("1250.50")));

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accounts).save(accountCaptor.capture());
        Account account = accountCaptor.getValue();
        assertEquals(new BigDecimal("1250.50"), account.getBalance());
        assertEquals("SAVINGS", account.getAccountType());
        assertEquals("ACTIVE", account.getStatus());
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactions).save(transactionCaptor.capture());
        assertEquals(account, transactionCaptor.getValue().getAccount());
        assertEquals("DEPOSIT", transactionCaptor.getValue().getTransactionType());
        assertEquals(new BigDecimal("1250.50"), transactionCaptor.getValue().getAmount());
        assertEquals("Initial deposit", transactionCaptor.getValue().getDescription());
        assertNull(transactionCaptor.getValue().getReceiverAccount());
        ArgumentCaptor<TransactionLog> logCaptor = ArgumentCaptor.forClass(TransactionLog.class);
        verify(logs).save(logCaptor.capture());
        assertEquals(transactionCaptor.getValue(), logCaptor.getValue().getTransaction());
        assertEquals("DEPOSIT", logCaptor.getValue().getAction());
    }

    @Test
    void negativeInitialDepositIsRejected() {
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(branches.findById(23)).thenReturn(Optional.of(new Branch()));

        BadRequest error = assertThrows(BadRequest.class,
                () -> service(23).register(request(new BigDecimal("-1"))));

        assertEquals("Initial deposit must be at least 0", error.getMessage());
        verify(users, never()).save(any(User.class));
        verify(customers, never()).save(any(Customer.class));
        verify(accounts, never()).save(any(Account.class));
    }

    @Test
    void initialDepositBelowAccountTypeMinimumIsRejected() {
        AccountType savings = new AccountType();
        savings.setTypeName("Savings Account");
        savings.setMinimumBalance(new BigDecimal("500.00"));
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(branches.findById(23)).thenReturn(Optional.of(new Branch()));
        when(accountTypes.findAll()).thenReturn(List.of(savings));

        BadRequest error = assertThrows(BadRequest.class,
                () -> service(23).register(request(new BigDecimal("250.00"))));

        assertEquals("Initial deposit must be at least 500.00 for Savings Account", error.getMessage());
        verify(users, never()).save(any(User.class));
        verify(customers, never()).save(any(Customer.class));
    }

    @Test
    void loginUpdatesLastLoginAndReturnsPreviousValue() {
        LocalDateTime previous = LocalDateTime.of(2025, 2, 3, 4, 5);
        User user = new User();
        user.setUserId(42);
        user.setEmail("customer@example.test");
        user.setRole("CUSTOMER");
        user.setPasswordHash("encoded-password");
        user.setLastLogin(previous);
        when(users.findByEmailIgnoreCase("customer@example.test")).thenReturn(Optional.of(user));
        when(encoder.matches("Password123", "encoded-password")).thenReturn(true);
        when(jwt.create(42, "customer@example.test", "CUSTOMER")).thenReturn("token");

        var response = service(23).login(new com.vaultcore.vbms.dto.AuthDtos.LoginRequest(
                "customer@example.test", "Password123"));

        assertEquals(previous, response.lastLogin());
        assertNotNull(user.getLastLogin());
        org.junit.jupiter.api.Assertions.assertNotEquals(previous, user.getLastLogin());
        verify(users).save(user);
    }

    @Test
    void missingConfiguredBranchReturnsClearBadRequestBeforeCreatingRecords() {
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(branches.findById(23)).thenReturn(Optional.empty());

        BadRequest error = assertThrows(BadRequest.class, () -> service(23).register(request()));

        assertEquals("Default registration branch is not configured", error.getMessage());
        verify(users, never()).save(any(User.class));
        verify(profiles, never()).save(any(UserProfile.class));
        verify(customers, never()).save(any(Customer.class));
    }

    @Test
    void duplicateEmailIsRejectedBeforeBranchLookupOrWrites() {
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(true);

        BadRequest error = assertThrows(BadRequest.class, () -> service(23).register(request()));

        assertEquals("Email is already registered", error.getMessage());
        verifyNoInteractions(branches, profiles, customers, encoder);
        verify(users, never()).save(any(User.class));
    }

    private AuthService service(Integer defaultBranchId) {
        TransactionService transactionService = new TransactionService(accounts, transactions, transfers, logs, authorization);
        AccountService accountService = new AccountService(accounts, accountTypes, customers, profiles, transactionService);
        return new AuthService(users, profiles, customers, branches, encoder, jwt, accountService, defaultBranchId);
    }

    private RegisterRequest request() {
        return request(BigDecimal.ZERO);
    }

    private RegisterRequest request(BigDecimal initialDeposit) {
        return new RegisterRequest(
                "customer@example.test",
                "Password123",
                "Vault",
                "Customer",
                "5551234567",
                LocalDate.of(1990, 1, 1),
                "UNSPECIFIED",
                "1 Main Street",
                initialDeposit,
                "SAVINGS");
    }

    private void prepareRegistration() {
        Branch branch = new Branch();
        when(users.existsByEmailIgnoreCase("customer@example.test")).thenReturn(false);
        when(branches.findById(23)).thenReturn(Optional.of(branch));
        when(users.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUserId(42);
            return user;
        });
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setCustomerId(84);
            return customer;
        });
        when(accountTypes.findAll()).thenReturn(List.of());
        when(accounts.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(encoder.encode("Password123")).thenReturn("encoded-password");
    }
}