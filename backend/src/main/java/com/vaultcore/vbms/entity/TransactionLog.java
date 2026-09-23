package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="transaction_logs") @Getter @Setter @NoArgsConstructor
public class TransactionLog {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="log_id") private Integer logId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="transaction_id", nullable=false) private Transaction transaction;
    @Column(name="performed_by") private Integer performedBy;
    private String action;
    @Column(name="log_time") private LocalDateTime logTime;
    private String remarks;
}
