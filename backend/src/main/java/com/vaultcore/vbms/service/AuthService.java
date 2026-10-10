package com.vaultcore.vbms.service;
import com.vaultcore.vbms.dto.AuthDtos.*;
import com.vaultcore.vbms.entity.*;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.repository.*;
import com.vaultcore.vbms.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Service
public class AuthService {
    private final UserRepository users; private final UserProfileRepository profiles; private final CustomerRepository customers; private final BranchRepository branches; private final BCryptPasswordEncoder encoder; private final JwtService jwt; private final AccountService accountService; private final Integer defaultRegistrationBranchId;
    public AuthService(UserRepository u,UserProfileRepository p,CustomerRepository c,BranchRepository b,BCryptPasswordEncoder e,JwtService j,
            AccountService accountService,
            @Value("${vbms.registration.default-branch-id:1}") Integer defaultRegistrationBranchId){users=u;profiles=p;customers=c;branches=b;encoder=e;jwt=j;this.accountService=accountService;this.defaultRegistrationBranchId=defaultRegistrationBranchId;}
    @Transactional public void register(RegisterRequest r) {
        if(users.existsByEmailIgnoreCase(r.email())) throw new BadRequest("Email is already registered");
        Branch branch = branches.findById(defaultRegistrationBranchId)
                .orElseThrow(() -> new BadRequest("Default registration branch is not configured"));
        if (r.initialDeposit() == null || r.initialDeposit().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequest("Initial deposit must be at least 0");
        }
        String accountType = r.accountType() == null || r.accountType().isBlank() ? "SAVINGS" : r.accountType();
        accountService.validateAccountTypeAndDeposit(accountType, r.initialDeposit());
        User u=new User();u.setEmail(r.email().toLowerCase());u.setPasswordHash(encoder.encode(r.password()));u.setRole("CUSTOMER");u.setVerified(true);u.setCreatedAt(LocalDateTime.now());users.save(u);UserProfile p=new UserProfile();p.setUser(u);p.setFirstName(r.firstName());p.setLastName(r.lastName());p.setPhone(r.phone());p.setDob(r.dob());p.setGender(r.gender());p.setAddress(r.address());profiles.save(p);Customer c=new Customer();c.setUser(u);c.setBranch(branch);c.setCustomerNumber("CUST"+String.format("%06d",u.getUserId()));c.setCreatedAt(LocalDateTime.now());c=customers.save(c);
        accountService.create(c, accountType, r.initialDeposit(), u.getUserId());
    }
    @Transactional public LoginResponse login(LoginRequest r) {
        User u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new BadRequest("Invalid credentials"));
        if(!encoder.matches(r.password(),u.getPasswordHash())) throw new BadRequest("Invalid credentials");
        LocalDateTime previousLastLogin=u.getLastLogin();
        u.setLastLogin(LocalDateTime.now());
        users.save(u);
        return new LoginResponse(jwt.create(u.getUserId(),u.getEmail(),u.getRole()),u.getUserId(),u.getEmail(),u.getRole(),previousLastLogin);
    }
}
