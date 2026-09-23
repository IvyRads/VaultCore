package com.vaultcore.vbms.service;
import com.vaultcore.vbms.dto.TransactionDtos.*;
import com.vaultcore.vbms.entity.*;
import com.vaultcore.vbms.exception.ApiExceptions.*;
import com.vaultcore.vbms.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
@Service
public class TransactionService {
    private final AccountRepository accounts; private final TransactionRepository transactions; private final TransferRepository transfers; private final TransactionLogRepository logs;
    public TransactionService(AccountRepository a,TransactionRepository t,TransferRepository tr,TransactionLogRepository l){accounts=a;transactions=t;transfers=tr;logs=l;}
    @Transactional public Transaction deposit(Integer id, MoneyRequest r, Integer actor){Account a=account(id);active(a);a.setBalance(a.getBalance().add(r.amount()));accounts.save(a);return record(a,"DEPOSIT",r.amount(),r.description(),actor);}
    @Transactional public Transaction withdraw(Integer id, MoneyRequest r, Integer actor){Account a=account(id);active(a);if(a.getBalance().compareTo(r.amount())<0)throw new InsufficientBalance();a.setBalance(a.getBalance().subtract(r.amount()));accounts.save(a);return record(a,"WITHDRAWAL",r.amount(),r.description(),actor);}
    @Transactional public Transfer transfer(TransferRequest r,Integer actor){if(r.senderAccountId().equals(r.receiverAccountId()))throw new BadRequest("Sender and receiver must differ");Integer first=Math.min(r.senderAccountId(),r.receiverAccountId()),second=Math.max(r.senderAccountId(),r.receiverAccountId());Account a=accounts.findForUpdate(first).orElseThrow(()->new NotFound("Account not found"));Account b=accounts.findForUpdate(second).orElseThrow(()->new NotFound("Account not found"));Account sender=a.getAccountId().equals(r.senderAccountId())?a:b;Account receiver=sender==a?b:a;active(sender);active(receiver);if(sender.getBalance().compareTo(r.amount())<0)throw new InsufficientBalance();sender.setBalance(sender.getBalance().subtract(r.amount()));receiver.setBalance(receiver.getBalance().add(r.amount()));Transaction out=record(sender,"TRANSFER_OUT",r.amount(),"Transfer to "+receiver.getAccountNumber(),actor);record(receiver,"TRANSFER_IN",r.amount(),"Transfer from "+sender.getAccountNumber(),actor);Transfer tr=new Transfer();tr.setSenderAccount(sender);tr.setReceiverAccount(receiver);tr.setAmount(r.amount());tr.setTransferDate(LocalDateTime.now());tr.setStatus("COMPLETED");transfers.save(tr);return tr;}
    private Account account(Integer id){return accounts.findForUpdate(id).orElseThrow(()->new NotFound("Account not found"));} private void active(Account a){if(!"ACTIVE".equalsIgnoreCase(a.getStatus()))throw new BadRequest("Account is not active");}
    private Transaction record(Account a,String type,java.math.BigDecimal amount,String desc,Integer actor){Transaction t=new Transaction();t.setAccount(a);t.setTransactionType(type);t.setAmount(amount);t.setDescription(desc);t.setTransactionDate(LocalDateTime.now());transactions.save(t);TransactionLog l=new TransactionLog();l.setTransaction(t);l.setPerformedBy(actor);l.setAction(type);l.setLogTime(LocalDateTime.now());l.setRemarks(desc);logs.save(l);return t;}
}
