package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="transactions") @Getter @Setter @NoArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="transaction_id") private Integer transactionId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="account_id", nullable=false) private Account account;
    @Column(name="transaction_type") private String transactionType;
    private BigDecimal amount;
    @Column(name="receiver_account") private String receiverAccount;
    private String description;
    @Column(name="transaction_date") private LocalDateTime transactionDate;
}
