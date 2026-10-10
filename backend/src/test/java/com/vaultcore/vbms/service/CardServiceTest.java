package com.vaultcore.vbms.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaultcore.vbms.dto.CardDtos.CardResponse;
import com.vaultcore.vbms.dto.CardDtos.CardCreateRequest;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.Card;
import com.vaultcore.vbms.entity.Customer;
import com.vaultcore.vbms.repository.AccountRequestRepository;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.CardRepository;
import com.vaultcore.vbms.repository.CustomerRepository;
import com.vaultcore.vbms.repository.UserRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.Forbidden;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock private CardRepository cards;
    @Mock private AccountRequestRepository requests;
    @Mock private AccountRepository accounts;
    @Mock private UserRepository users;
    @Mock private CustomerRepository customers;

    @Test
    void listReturnsMaskedCardNumberAndOnlySafeCardFields() throws Exception {
        Card card = card();
        when(cards.findByAccountCustomerCustomerIdOrderByCardIdDesc(10)).thenReturn(List.of(card));

        List<CardResponse> result = service().list(new Caller(1, 10, false));

        assertEquals(1, result.size());
        assertEquals(7, result.get(0).cardId());
        assertEquals("•••• •••• •••• 1234", result.get(0).cardNumber());
        assertEquals("DEBIT", result.get(0).cardType());
        assertEquals(LocalDate.of(2030, 12, 31), result.get(0).expiryDate());
        assertEquals("ACTIVE", result.get(0).status());

        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(result.get(0));
        assertFalse(json.contains("4111111111111234"));
        assertFalse(json.toLowerCase().contains("cvv"));
        assertFalse(json.toLowerCase().contains("pin"));
    }

    @Test
    void blockResponseAlsoContainsOnlyMaskedCardNumber() throws Exception {
        Card card = card();
        when(cards.findForUpdate(7)).thenReturn(Optional.of(card));
        when(cards.save(card)).thenReturn(card);

        CardResponse result = service().changeStatus(7, "BLOCK", "Lost Card", null, new Caller(1, 10, false));

        assertEquals("•••• •••• •••• 1234", result.cardNumber());
        assertEquals("BLOCKED", result.status());
        assertCardResponseDoesNotContainFullNumber(result);
        verify(requests).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void unblockResponseDoesNotContainFullCardNumber() throws Exception {
        Card card = card();
        card.setStatus("BLOCKED");
        when(cards.findForUpdate(7)).thenReturn(Optional.of(card));
        when(cards.save(card)).thenReturn(card);

        CardResponse result = service().changeStatus(7, "UNBLOCK", "Card Found", null, new Caller(1, 10, false));

        assertEquals("ACTIVE", result.status());
        assertCardResponseDoesNotContainFullNumber(result);
    }

    @Test
    void nullAndShortCardNumbersDoNotThrowOrRevealTheValue() {
        Card shortCard = card();
        shortCard.setCardNumber("123");
        Card nullCard = card();
        nullCard.setCardNumber(null);
        when(cards.findByAccountCustomerCustomerIdOrderByCardIdDesc(10)).thenReturn(List.of(shortCard, nullCard));

        List<CardResponse> result = assertDoesNotThrow(() -> service().list(new Caller(1, 10, false)));

        assertEquals("•••• •••• •••• ", result.get(0).cardNumber());
        assertEquals("•••• •••• •••• ", result.get(1).cardNumber());
    }

    @Test
    void addGeneratesLuhnNumberAndReturnsOnlyMaskedCardNumber() throws Exception {
        Account account = account("ACTIVE", 10);
        account.setAccountNumber("000123456789");
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account));
        when(cards.countByAccountAccountId(42)).thenReturn(0L);
        when(cards.existsByCardNumber(any())).thenReturn(false);
        when(cards.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setCardId(8);
            return card;
        });

        CardResponse result = service().add(new CardCreateRequest(42, "Debit"), new Caller(1, 10, false));

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cards).save(cardCaptor.capture());
        String generatedNumber = cardCaptor.getValue().getCardNumber();
        assertEquals(16, generatedNumber.length());
        assertTrue(luhnValid(generatedNumber));
        assertEquals("•••• •••• •••• " + generatedNumber.substring(12), result.cardNumber());
        assertEquals("000123456789", result.accountNumber());
        assertEquals("DEBIT", result.cardType());
        assertEquals(LocalDate.now().plusYears(5), result.expiryDate());
        assertEquals("ACTIVE", result.status());
        assertNotNull(generatedNumber);
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(result);
        assertFalse(json.contains(generatedNumber));
        assertFalse(json.toLowerCase().contains("cvv"));
        assertFalse(json.toLowerCase().contains("pin"));
    }

    @Test
    void addRejectsAnotherCustomersAccount() {
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account("ACTIVE", 10)));

        assertThrows(Forbidden.class, () -> service().add(
                new CardCreateRequest(42, "DEBIT"), new Caller(2, 20, false)));

        verify(cards, never()).save(any(Card.class));
        verify(cards, never()).countByAccountAccountId(42);
    }

    @Test
    void addRejectsInactiveAccount() {
        when(accounts.findForUpdate(42)).thenReturn(Optional.of(account("FROZEN", 10)));

        assertThrows(BadRequest.class, () -> service().add(
                new CardCreateRequest(42, "CREDIT"), new Caller(1, 10, false)));

        verify(cards, never()).save(any(Card.class));
        verify(cards, never()).countByAccountAccountId(42);
    }

    private CardService service() {
        AuthorizationService authorization = new AuthorizationService(users, customers);
        return new CardService(cards, requests, authorization, accounts);
    }

    private void assertCardResponseDoesNotContainFullNumber(CardResponse response) throws Exception {
        String json = new ObjectMapper().findAndRegisterModules().writeValueAsString(response);
        assertFalse(json.contains("4111111111111234"));
        assertFalse(json.toLowerCase().contains("cvv"));
        assertFalse(json.toLowerCase().contains("pin"));
    }

    private Card card() {
        Account account = new Account();
        account.setAccountId(42);
        Customer customer = new Customer();
        customer.setCustomerId(10);
        account.setCustomer(customer);
        Card card = new Card();
        card.setCardId(7);
        card.setAccount(account);
        card.setCardNumber("4111111111111234");
        card.setCardType("DEBIT");
        card.setExpiryDate(LocalDate.of(2030, 12, 31));
        card.setStatus("ACTIVE");
        return card;
    }

    private Account account(String status, Integer customerId) {
        Account account = new Account();
        account.setAccountId(42);
        account.setAccountNumber("000123456789");
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        account.setCustomer(customer);
        account.setStatus(status);
        return account;
    }

    private boolean luhnValid(String number) {
        int sum = 0;
        boolean doubleDigit = false;
        for (int index = number.length() - 1; index >= 0; index--) {
            int digit = number.charAt(index) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
