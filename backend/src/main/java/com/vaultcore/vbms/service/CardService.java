package com.vaultcore.vbms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.entity.AccountRequest;
import com.vaultcore.vbms.entity.Card;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRequestRepository;
import com.vaultcore.vbms.repository.CardRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@Service
public class CardService {
    private final CardRepository cards;
    private final AccountRequestRepository requests;
    private final AuthorizationService authorization;

    public CardService(CardRepository cards, AccountRequestRepository requests, AuthorizationService authorization) {
        this.cards = cards;
        this.requests = requests;
        this.authorization = authorization;
    }

    public List<Card> list(Caller caller) {
        return caller.admin() ? cards.findAll() : cards.findByAccountCustomerCustomerIdOrderByCardIdDesc(caller.requireCustomerId());
    }

    @Transactional
    public Card changeStatus(Integer cardId, String action, String reason, String remarks, Caller caller) {
        Card card = cards.findForUpdate(cardId).orElseThrow(() -> new NotFound("Card not found"));
        authorization.requireAccountAccess(card.getAccount(), caller);
        String targetStatus;
        if ("BLOCK".equalsIgnoreCase(action)) targetStatus = "BLOCKED";
        else if ("UNBLOCK".equalsIgnoreCase(action)) targetStatus = "ACTIVE";
        else throw new BadRequest("Action must be BLOCK or UNBLOCK");
        card.setStatus(targetStatus);
        Card saved = cards.save(card);
        AccountRequest request = new AccountRequest();
        request.setAccount(card.getAccount());
        request.setAction(action.toUpperCase());
        request.setReason(reason);
        request.setRequestDate(LocalDateTime.now());
        request.setStatus("COMPLETED");
        requests.save(request);
        return saved;
    }
}
