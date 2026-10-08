package com.vaultcore.vbms.security;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.exception.ApiExceptions.Forbidden;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;

@Service
public class AuthorizationService {
    private final UserRepository users;
    private final CustomerRepository customers;

    public AuthorizationService(UserRepository users, CustomerRepository customers) {
        this.users = users;
        this.customers = customers;
    }

    public Caller caller(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new Forbidden("Authentication is required");
        }

        var user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new NotFound("User not found"));
        Integer customerId = customers.findByUserUserId(user.getUserId())
                .map(customer -> customer.getCustomerId())
                .orElse(null);
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!admin && customerId == null) {
            throw new Forbidden("A customer profile is required");
        }

        return new Caller(user.getUserId(), customerId, admin);
    }

    public void requireAccountAccess(Account account, Caller caller) {
        requireCustomerAccess(account.getCustomer().getCustomerId(), caller);
    }

    public void requireCustomerAccess(Integer resourceCustomerId, Caller caller) {
        if (!caller.admin() && !Objects.equals(caller.customerId(), resourceCustomerId)) {
            throw new Forbidden("You are not authorized to access this resource");
        }
    }

    public record Caller(Integer userId, Integer customerId, boolean admin) {
        public int requireCustomerId() {
            if (customerId == null) {
                throw new Forbidden("A customer profile is required");
            }
            return customerId;
        }
    }
}
