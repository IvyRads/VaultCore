package com.vaultcore.vbms.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public final class TransactionDtos {
    private TransactionDtos() { }
    public record MoneyRequest(@NotNull Integer accountId, @NotNull @Positive BigDecimal amount, String description) { }
    public record TransferRequest(@NotNull Integer senderAccountId, @NotNull Integer receiverAccountId, @NotNull @Positive BigDecimal amount) { }
}
