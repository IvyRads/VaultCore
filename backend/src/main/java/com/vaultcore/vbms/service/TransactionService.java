package com.vaultcore.vbms.service;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vaultcore.vbms.dto.TransactionDtos.MoneyRequest;
import com.vaultcore.vbms.dto.TransactionDtos.TransferRequest;
import com.vaultcore.vbms.dto.TransactionDtos.TransferResponse;
import com.vaultcore.vbms.dto.TransactionDtos.TransactionMonitoringResponse;
import com.vaultcore.vbms.dto.TransactionDtos.TransactionResponse;
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
        return record(account, "DEPOSIT", request.amount(), request.description(), caller.userId(), null);
    }

    @Transactional
    public Transaction withdraw(int id, MoneyRequest request, Caller caller) {
        Account account = account(id);
        authorization.requireAccountAccess(account, caller);
        active(account);
        if (account.getBalance().compareTo(request.amount()) < 0) throw new InsufficientBalance();
        account.setBalance(account.getBalance().subtract(request.amount()));
        accounts.save(account);
        return record(account, "WITHDRAWAL", request.amount(), request.description(), caller.userId(), null);
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
        record(sender, "TRANSFER_OUT", request.amount(), "Transfer to " + receiver.getAccountNumber(), caller.userId(),
                receiver.getAccountNumber());
        record(receiver, "TRANSFER_IN", request.amount(), "Transfer from " + sender.getAccountNumber(), caller.userId(),
                sender.getAccountNumber());
        Transfer transfer = new Transfer();
        transfer.setSenderAccount(sender);
        transfer.setReceiverAccount(receiver);
        transfer.setAmount(request.amount());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("COMPLETED");
        return transferResponse(transfers.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> accountTransactions(int accountId, Caller caller) {
        Account account = accounts.findById(accountId).orElseThrow(() -> new NotFound("Account not found"));
        authorization.requireAccountAccess(account, caller);
        return transactions.findByAccountAccountIdOrderByTransactionDateDesc(accountId).stream()
                .map(this::transactionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> customerTransactions(int customerId, Caller caller) {
        authorization.requireCustomerAccess(customerId, caller);
        return transactions.findByAccountCustomerCustomerIdOrderByTransactionDateDesc(customerId).stream()
                .map(this::transactionResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> myTransactions(Caller caller) {
        return transactions.findByAccountCustomerCustomerIdOrderByTransactionDateDesc(caller.requireCustomerId()).stream()
                .map(this::transactionResponse)
                .toList();
    }

    @Transactional
    public Transaction recordInitialDeposit(Account account, java.math.BigDecimal amount, Integer actor) {
        if (amount == null || amount.signum() <= 0) {
            throw new BadRequest("Initial deposit must be greater than 0");
        }
        return record(account, "DEPOSIT", amount, "Initial deposit", actor, null);
    }

    @Transactional(readOnly = true)
    public List<TransactionMonitoringResponse> monitorTransactions(String transactionType, Integer accountId,
            LocalDateTime from, LocalDateTime to) {
        return transactions.findForMonitoring(transactionType, accountId, from, to).stream()
                .map(this::transactionMonitoringResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransactionMonitoringResponse monitorTransaction(Integer transactionId) {
        Transaction transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new NotFound("Transaction not found"));
        return transactionMonitoringResponse(transaction);
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

    private Transaction record(Account account, String type, java.math.BigDecimal amount, String description, Integer actor,
            String receiverAccount) {
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setReceiverAccount(receiverAccount);
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

    private TransactionMonitoringResponse transactionMonitoringResponse(Transaction transaction) {
        Account account = transaction.getAccount();
        return new TransactionMonitoringResponse(
                transaction.getTransactionId(),
                account.getAccountId(),
                account.getAccountNumber(),
                account.getCustomer().getCustomerId(),
                account.getCustomer().getCustomerNumber(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getReceiverAccount(),
                transaction.getDescription(),
                transaction.getTransactionDate());
    }

    private TransactionResponse transactionResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getTransactionId(), transaction.getAccount().getAccountId(),
                transaction.getAccount().getAccountNumber(), transaction.getTransactionType(),
                transaction.getAmount(), transaction.getReceiverAccount(), transaction.getDescription(),
                transaction.getTransactionDate());
    }
}
