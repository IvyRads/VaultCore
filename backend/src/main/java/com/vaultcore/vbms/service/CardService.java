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

@Service
public class CardService {
    private final CardRepository cards;
    private final AccountRequestRepository requests;

    public CardService(CardRepository cards, AccountRequestRepository requests) {
        this.cards = cards;
        this.requests = requests;
    }

    public List<Card> list(Integer customerId) {
        return cards.findByAccountCustomerCustomerIdOrderByCardIdDesc(customerId);
    }

    @Transactional
    public Card changeStatus(Integer cardId, String action, String reason, String remarks, Integer customerId) {
        Card card = cards.findForUpdate(cardId).orElseThrow(() -> new NotFound("Card not found"));
        if (!card.getAccount().getCustomer().getCustomerId().equals(customerId)) {
            throw new NotFound("Card not found");
        }
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
