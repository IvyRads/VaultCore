package com.vaultcore.vbms.controller;

import com.vaultcore.vbms.entity.SupportTicket;
import com.vaultcore.vbms.repository.*;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.security.AuthorizationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController @RequestMapping("/api/support/tickets")
public class SupportTicketController {
    private final SupportTicketRepository tickets; private final CustomerRepository customers; private final AuthorizationService authorization;
    public SupportTicketController(SupportTicketRepository t,CustomerRepository c,AuthorizationService authorization){tickets=t;customers=c;this.authorization=authorization;}
    public record TicketRequest(@NotBlank String subject,@NotBlank String issue){}
    @PostMapping public SupportTicket create(@Valid @RequestBody TicketRequest request,Authentication authentication){var caller=authorization.caller(authentication);var customer=customers.findById(caller.requireCustomerId()).orElseThrow(()->new NotFound("Customer not found"));SupportTicket ticket=new SupportTicket();ticket.setCustomer(customer);ticket.setSubject(request.subject());ticket.setIssue(request.issue());ticket.setStatus("OPEN");ticket.setCreatedAt(LocalDateTime.now());return tickets.save(ticket);}
    @GetMapping public Object list(Authentication authentication){var caller=authorization.caller(authentication);return caller.admin()?tickets.findAll():tickets.findByCustomerCustomerIdOrderByCreatedAtDesc(caller.requireCustomerId());}
    @GetMapping("/{id}") public SupportTicket get(@PathVariable int id,Authentication authentication){var caller=authorization.caller(authentication);var ticket=tickets.findWithCustomerByTicketId(id).orElseThrow(()->new NotFound("Support ticket not found"));authorization.requireCustomerAccess(ticket.getCustomer().getCustomerId(),caller);return ticket;}
}
