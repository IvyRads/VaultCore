package com.vaultcore.vbms.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CardDtos {
    private CardDtos() { }

    public record CardCreateRequest(@NotNull Integer accountId, @NotBlank String cardType) { }

    public record CardResponse(Integer cardId, String cardNumber, String cardType, LocalDate expiryDate, String status,
            String accountNumber) { }
}
