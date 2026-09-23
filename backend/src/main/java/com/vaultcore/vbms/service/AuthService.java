package com.vaultcore.vbms.service;
import com.vaultcore.vbms.dto.AuthDtos.*;
import com.vaultcore.vbms.entity.*;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.repository.*;
import com.vaultcore.vbms.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
public class AuthService {
    private final UserRepository users; private final UserProfileRepository profiles; private final CustomerRepository customers; private final BCryptPasswordEncoder encoder; private final JwtService jwt;
    public AuthService(UserRepository u,UserProfileRepository p,CustomerRepository c,BCryptPasswordEncoder e,JwtService j){users=u;profiles=p;customers=c;encoder=e;jwt=j;}
    @Transactional public void register(RegisterRequest r) { if(users.existsByEmailIgnoreCase(r.email())) throw new BadRequest("Email is already registered"); User u=new User();u.setEmail(r.email().toLowerCase());u.setPasswordHash(encoder.encode(r.password()));u.setRole("CUSTOMER");u.setVerified(true);u.setCreatedAt(LocalDateTime.now());users.save(u);UserProfile p=new UserProfile();p.setUser(u);p.setFirstName(r.firstName());p.setLastName(r.lastName());p.setPhone(r.phone());p.setDob(r.dob());p.setGender(r.gender());p.setAddress(r.address());profiles.save(p);Customer c=new Customer();c.setUser(u);c.setCustomerNumber("CUST"+String.format("%06d",u.getUserId()));c.setCreatedAt(LocalDateTime.now());customers.save(c);}
    public LoginResponse login(LoginRequest r) { User u=users.findByEmailIgnoreCase(r.email()).orElseThrow(()->new BadRequest("Invalid credentials")); if(!encoder.matches(r.password(),u.getPasswordHash())) throw new BadRequest("Invalid credentials"); return new LoginResponse(jwt.create(u.getUserId(),u.getEmail(),u.getRole()),u.getUserId(),u.getEmail(),u.getRole()); }
}
