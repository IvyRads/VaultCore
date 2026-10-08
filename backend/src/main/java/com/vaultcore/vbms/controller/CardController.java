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
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.service.CardService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService service;
    private final AuthorizationService authorization;

    public CardController(CardService service, AuthorizationService authorization) {
        this.service = service;
        this.authorization = authorization;
    }

    public record CardActionRequest(@NotBlank String reason, String remarks) {}

    @GetMapping
    public List<Card> list(Authentication authentication) {
        return service.list(authorization.caller(authentication));
    }

    @PostMapping("/{id}/block")
    public Card block(@PathVariable Integer id, @Valid @RequestBody CardActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "BLOCK", request.reason(), request.remarks(), authorization.caller(authentication));
    }

    @PostMapping("/{id}/unblock")
    public Card unblock(@PathVariable Integer id, @Valid @RequestBody CardActionRequest request, Authentication authentication) {
        return service.changeStatus(id, "UNBLOCK", request.reason(), request.remarks(), authorization.caller(authentication));
    }
}
