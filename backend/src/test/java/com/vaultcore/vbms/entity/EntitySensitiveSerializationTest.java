package com.vaultcore.vbms.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class EntitySensitiveSerializationTest {
    private static final String CARD_NUMBER = "4111111111111234";
    private static final String PASSWORD_HASH = "$2a$10$privatehash";

    @Test
    void accountTransactionAndFraudReportDoNotExposeCardNumberOrPasswordHash() throws Exception {
        User user = new User();
        user.setEmail("customer@example.test");
        user.setPasswordHash(PASSWORD_HASH);
        Customer customer = new Customer();
        customer.setUser(user);

        Account account = new Account();
        account.setCustomer(customer);
        Card card = new Card();
        card.setAccount(account);
        card.setCardNumber(CARD_NUMBER);

        Transaction transaction = new Transaction();
        transaction.setAccount(account);

        FraudReport report = new FraudReport();
        report.setCustomer(customer);
        report.setAccount(account);

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        assertSensitiveValuesExcluded(mapper.writeValueAsString(account));
        assertSensitiveValuesExcluded(mapper.writeValueAsString(transaction));
        assertSensitiveValuesExcluded(mapper.writeValueAsString(report));
        assertSensitiveValuesExcluded(mapper.writeValueAsString(card));
    }

    private void assertSensitiveValuesExcluded(String json) {
        assertFalse(json.contains(CARD_NUMBER));
        assertFalse(json.contains(PASSWORD_HASH));
        assertFalse(json.contains("passwordHash"));
    }
}
