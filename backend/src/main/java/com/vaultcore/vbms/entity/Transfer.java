package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="transfers") @Getter @Setter @NoArgsConstructor
public class Transfer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="transfer_id") private Integer transferId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="sender_account_id", nullable=false) private Account senderAccount;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="receiver_account_id", nullable=false) private Account receiverAccount;
    private BigDecimal amount;
    @Column(name="transfer_date") private LocalDateTime transferDate;
    private String status;
}
