package com.vaultcore.vbms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultcore.vbms.entity.User;
import com.vaultcore.vbms.entity.UserProfile;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.BranchRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.TransactionRepository;
import com.vaultcore.vbms.repository.UserProfileRepository;
import com.vaultcore.vbms.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AdminSummaryServiceTest {
    @Mock private CustomerRepository customers;
    @Mock private BranchRepository branches;
    @Mock private AccountRepository accounts;
    @Mock private TransactionRepository transactions;
    @Mock private UserRepository users;
    @Mock private UserProfileRepository profiles;
    @InjectMocks private AdminSummaryService service;

    @Test
    void summaryReturnsCountsAndAdminDetails() {
        User admin = new User();
        admin.setUserId(9);
        admin.setEmail("admin@vaultcore.test");
        admin.setLastLogin(LocalDateTime.of(2026, 10, 9, 10, 30));
        UserProfile profile = new UserProfile();
        profile.setFirstName("Vault");
        profile.setLastName("Admin");
        when(users.findByEmailIgnoreCase(admin.getEmail())).thenReturn(Optional.of(admin));
        when(profiles.findByUserUserId(9)).thenReturn(Optional.of(profile));
        when(customers.count()).thenReturn(12L);
        when(branches.count()).thenReturn(3L);
        when(accounts.count()).thenReturn(20L);
        when(accounts.countByStatusIgnoreCase("ACTIVE")).thenReturn(17L);
        when(transactions.count()).thenReturn(45L);

        var result = service.summary(admin.getEmail());

        assertEquals(12L, result.totalCustomers());
        assertEquals(3L, result.totalBranches());
        assertEquals(20L, result.totalAccounts());
        assertEquals(17L, result.activeAccounts());
        assertEquals(45L, result.totalTransactions());
        assertEquals("Operational", result.bankStatus());
        assertEquals(9, result.adminUserId());
        assertEquals("admin@vaultcore.test", result.adminEmail());
        assertEquals("Vault Admin", result.adminName());
        assertEquals("2026-10-09T10:30", result.lastLogin());
    }
}
