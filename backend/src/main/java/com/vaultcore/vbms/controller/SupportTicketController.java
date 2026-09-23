package com.vaultcore.vbms.controller;

import com.vaultcore.vbms.entity.SupportTicket;
import com.vaultcore.vbms.repository.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController @RequestMapping("/api/support/tickets")
public class SupportTicketController {
    private final SupportTicketRepository tickets; private final CustomerRepository customers; private final UserRepository users;
    public SupportTicketController(SupportTicketRepository t,CustomerRepository c,UserRepository u){tickets=t;customers=c;users=u;}
    public record TicketRequest(@NotBlank String subject,@NotBlank String issue){}
    @PostMapping public SupportTicket create(@RequestBody TicketRequest r,Authentication a){var user=users.findByEmailIgnoreCase(a.getName()).orElseThrow();var customer=customers.findByUserUserId(user.getUserId()).orElseThrow();SupportTicket t=new SupportTicket();t.setCustomer(customer);t.setSubject(r.subject());t.setIssue(r.issue());t.setStatus("OPEN");t.setCreatedAt(LocalDateTime.now());return tickets.save(t);}
    @GetMapping public Object list(Authentication a){var user=users.findByEmailIgnoreCase(a.getName()).orElseThrow();if(user.getRole().equals("ADMIN"))return tickets.findAll();return customers.findByUserUserId(user.getUserId()).map(c->tickets.findByCustomerCustomerIdOrderByCreatedAtDesc(c.getCustomerId())).orElseThrow();}
    @GetMapping("/{id}") public SupportTicket get(@PathVariable Integer id){return tickets.findById(id).orElseThrow();}
}
