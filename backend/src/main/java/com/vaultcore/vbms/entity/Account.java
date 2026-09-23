package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="accounts") @Getter @Setter @NoArgsConstructor
public class Account {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="account_id") private Integer accountId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    @Column(name="account_number") private String accountNumber;
    @Column(name="account_type") private String accountType;
    private BigDecimal balance;
    private String status;
    @Column(name="created_at") private LocalDateTime createdAt;
}
