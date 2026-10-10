package com.vaultcore.vbms.service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.security.SecureRandom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.CardDtos.CardResponse;
import com.vaultcore.vbms.dto.CardDtos.CardCreateRequest;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.AccountRequest;
import com.vaultcore.vbms.entity.Card;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRequestRepository;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.CardRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;

@Service
public class CardService {
    private final CardRepository cards;
    private final AccountRequestRepository requests;
    private final AuthorizationService authorization;
    private final AccountRepository accounts;
    private final SecureRandom random = new SecureRandom();

    public CardService(CardRepository cards, AccountRequestRepository requests, AuthorizationService authorization,
            AccountRepository accounts) {
        this.cards = cards;
        this.requests = requests;
        this.authorization = authorization;
        this.accounts = accounts;
    }

    @Transactional(readOnly = true)
    public List<CardResponse> list(Caller caller) {
        List<Card> customerCards = caller.admin()
                ? cards.findAll()
                : cards.findByAccountCustomerCustomerIdOrderByCardIdDesc(caller.requireCustomerId());
        return customerCards.stream().map(this::response).toList();
    }

    @Transactional
    public CardResponse add(CardCreateRequest request, Caller caller) {
        Account account = accounts.findForUpdate(request.accountId())
                .orElseThrow(() -> new NotFound("Account not found"));
        authorization.requireAccountAccess(account, caller);
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) {
            throw new BadRequest("Only active accounts can have a card added");
        }
        String type;
        if ("DEBIT".equalsIgnoreCase(request.cardType())) type = "DEBIT";
        else if ("CREDIT".equalsIgnoreCase(request.cardType())) type = "CREDIT";
        else throw new BadRequest("Card type must be Debit or Credit");
        if (cards.countByAccountAccountId(account.getAccountId()) >= 3) {
            throw new BadRequest("An account can have at most 3 cards");
        }

        Card card = new Card();
        card.setAccount(account);
        card.setCardNumber(uniqueCardNumber());
        card.setCardType(type);
        card.setExpiryDate(LocalDate.now().plusYears(5));
        card.setStatus("ACTIVE");
        return response(cards.save(card));
    }

    @Transactional
    public CardResponse changeStatus(Integer cardId, String action, String reason, String remarks, Caller caller) {
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
        return response(saved);
    }

    private CardResponse response(Card card) {
        String cardNumber = card.getCardNumber();
        String visibleDigits = cardNumber != null && cardNumber.length() > 4
                ? cardNumber.substring(cardNumber.length() - 4)
                : "";
        return new CardResponse(
                card.getCardId(),
                "•••• •••• •••• " + visibleDigits,
                card.getCardType(),
                card.getExpiryDate(),
                card.getStatus(),
                card.getAccount().getAccountNumber());
    }

    private String uniqueCardNumber() {
        for (int attempt = 0; attempt < 100; attempt++) {
            String candidate = generateCardNumber();
            if (!cards.existsByCardNumber(candidate)) return candidate;
        }
        throw new BadRequest("Unable to generate a unique card number");
    }

    private String generateCardNumber() {
        StringBuilder number = new StringBuilder(16);
        number.append(1 + random.nextInt(9));
        for (int index = 1; index < 15; index++) number.append(random.nextInt(10));
        int sum = 0;
        for (int index = 14, position = 0; index >= 0; index--, position++) {
            int digit = number.charAt(index) - '0';
            if (position % 2 == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        number.append((10 - sum % 10) % 10);
        return number.toString();
    }
}
