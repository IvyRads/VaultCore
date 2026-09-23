package com.vaultcore.vbms.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vaultcore.vbms.entity.Card;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.service.CardService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService service;
    private final UserRepository users;
    private final CustomerRepository customers;

    public CardController(CardService service, UserRepository users, CustomerRepository customers) {
        this.service = service;
        this.users = users;
        this.customers = customers;
    }

    public record CardActionRequest(@NotBlank String reason, String remarks) {}

    @GetMapping
    public List<Card> list(Authentication authentication) {
        return service.list(customerId(authentication));
    }

    @PostMapping("/{id}/block")
    public Card block(@PathVariable Integer id, @Valid @RequestBody CardActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "BLOCK", request.reason(), request.remarks(), customerId(authentication));
    }

    @PostMapping("/{id}/unblock")
    public Card unblock(@PathVariable Integer id, @Valid @RequestBody CardActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "UNBLOCK", request.reason(), request.remarks(), customerId(authentication));
    }

    private Integer customerId(Authentication authentication) {
        var user = users.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
        return customers.findByUserUserId(user.getUserId()).orElseThrow().getCustomerId();
    }
}
