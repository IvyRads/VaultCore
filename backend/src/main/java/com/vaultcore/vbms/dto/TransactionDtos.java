package com.vaultcore.vbms.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
public final class TransactionDtos {
    private TransactionDtos() { }
    public record MoneyRequest(@NotNull Integer accountId, @NotNull @Positive BigDecimal amount, String description) { }
    public record TransferRequest(@NotNull Integer senderAccountId, @NotNull Integer receiverAccountId, @NotNull @Positive BigDecimal amount) { }
    public record TransferResponse(Integer transferId, Integer senderAccountId, Integer receiverAccountId,
        BigDecimal amount, LocalDateTime transferDate, String status) { }
    public record TransactionResponse(Integer transactionId, Integer accountId, String accountNumber,
        String transactionType, BigDecimal amount, String receiverAccount, String description,
        LocalDateTime transactionDate) { }
    public record TransactionMonitoringResponse(Integer transactionId, Integer accountId, String accountNumber,
        Integer customerId, String customerNumber, String transactionType, BigDecimal amount, String receiverAccount,
        String description, LocalDateTime transactionDate) { }
}
