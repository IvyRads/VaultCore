package com.vaultcore.vbms.service;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.TransactionDtos.MoneyRequest;
import com.vaultcore.vbms.dto.TransactionDtos.TransferRequest;
import com.vaultcore.vbms.dto.TransactionDtos.TransferResponse;
import com.vaultcore.vbms.entity.Account;
import com.vaultcore.vbms.entity.Transaction;
import com.vaultcore.vbms.entity.TransactionLog;
import com.vaultcore.vbms.entity.Transfer;
import com.vaultcore.vbms.exception.ApiExceptions.BadRequest;
import com.vaultcore.vbms.exception.ApiExceptions.InsufficientBalance;
import com.vaultcore.vbms.exception.ApiExceptions.NotFound;
import com.vaultcore.vbms.repository.AccountRepository;
import com.vaultcore.vbms.repository.TransactionLogRepository;
import com.vaultcore.vbms.repository.TransactionRepository;
import com.vaultcore.vbms.repository.TransferRepository;
import com.vaultcore.vbms.security.AuthorizationService;
import com.vaultcore.vbms.security.AuthorizationService.Caller;
import java.util.List;
@Service
public class TransactionService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final TransferRepository transfers;
    private final TransactionLogRepository logs;
    private final AuthorizationService authorization;

    public TransactionService(AccountRepository accounts, TransactionRepository transactions, TransferRepository transfers,
            TransactionLogRepository logs, AuthorizationService authorization) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.transfers = transfers;
        this.logs = logs;
        this.authorization = authorization;
    }

    @Transactional
    public Transaction deposit(int id, MoneyRequest request, Caller caller) {
        Account account = account(id);
        authorization.requireAccountAccess(account, caller);
        active(account);
        account.setBalance(account.getBalance().add(request.amount()));
        accounts.save(account);
        return record(account, "DEPOSIT", request.amount(), request.description(), caller.userId());
    }

    @Transactional
    public Transaction withdraw(int id, MoneyRequest request, Caller caller) {
        Account account = account(id);
        authorization.requireAccountAccess(account, caller);
        active(account);
        if (account.getBalance().compareTo(request.amount()) < 0) throw new InsufficientBalance();
        account.setBalance(account.getBalance().subtract(request.amount()));
        accounts.save(account);
        return record(account, "WITHDRAWAL", request.amount(), request.description(), caller.userId());
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request, Caller caller) {
        if (request.senderAccountId().equals(request.receiverAccountId())) {
            throw new BadRequest("Sender and receiver must differ");
        }
        Integer first = Math.min(request.senderAccountId(), request.receiverAccountId());
        Integer second = Math.max(request.senderAccountId(), request.receiverAccountId());
        Account firstAccount = accounts.findForUpdate(first).orElseThrow(() -> new NotFound("Account not found"));
        Account secondAccount = accounts.findForUpdate(second).orElseThrow(() -> new NotFound("Account not found"));
        Account sender = firstAccount.getAccountId().equals(request.senderAccountId()) ? firstAccount : secondAccount;
        Account receiver = sender == firstAccount ? secondAccount : firstAccount;
        authorization.requireAccountAccess(sender, caller);
        active(sender);
        active(receiver);
        if (sender.getBalance().compareTo(request.amount()) < 0) throw new InsufficientBalance();
        sender.setBalance(sender.getBalance().subtract(request.amount()));
        receiver.setBalance(receiver.getBalance().add(request.amount()));
        record(sender, "TRANSFER_OUT", request.amount(), "Transfer to " + receiver.getAccountNumber(), caller.userId());
        record(receiver, "TRANSFER_IN", request.amount(), "Transfer from " + sender.getAccountNumber(), caller.userId());
        Transfer transfer = new Transfer();
        transfer.setSenderAccount(sender);
        transfer.setReceiverAccount(receiver);
        transfer.setAmount(request.amount());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("COMPLETED");
        return transferResponse(transfers.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<Transaction> accountTransactions(int accountId, Caller caller) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new NotFound("Account not found"));
        authorization.requireAccountAccess(account, caller);
        return transactions.findByAccountAccountIdOrderByTransactionDateDesc(accountId);
    }

    @Transactional(readOnly = true)
    public List<Transaction> customerTransactions(int customerId, Caller caller) {
        authorization.requireCustomerAccess(customerId, caller);
        return transactions.findByAccountCustomerCustomerIdOrderByTransactionDateDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<TransferResponse> accountTransfers(int accountId, Caller caller) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new NotFound("Account not found"));
        authorization.requireAccountAccess(account, caller);
        return transfers.findBySenderAccountAccountIdOrReceiverAccountAccountIdOrderByTransferDateDesc(accountId, accountId)
                .stream()
                .map(this::transferResponse)
                .toList();
    }

    private Account account(int id) {
        return accounts.findForUpdate(id).orElseThrow(() -> new NotFound("Account not found"));
    }

    private void active(Account account) {
        if (!"ACTIVE".equalsIgnoreCase(account.getStatus())) throw new BadRequest("Account is not active");
    }

    private Transaction record(Account account, String type, java.math.BigDecimal amount, String description, Integer actor) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transactions.save(transaction);
        TransactionLog log = new TransactionLog();
        log.setTransaction(transaction);
        log.setPerformedBy(actor);
        log.setAction(type);
        log.setLogTime(LocalDateTime.now());
        log.setRemarks(description);
        logs.save(log);
        return transaction;
    }

    private TransferResponse transferResponse(Transfer transfer) {
        return new TransferResponse(
                transfer.getTransferId(),
                transfer.getSenderAccount().getAccountId(),
                transfer.getReceiverAccount().getAccountId(),
                transfer.getAmount(),
                transfer.getTransferDate(),
                transfer.getStatus());
    }
}
