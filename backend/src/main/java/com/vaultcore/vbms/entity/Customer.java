package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="customers") @Getter @Setter @NoArgsConstructor
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Integer customerId;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false) private User user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="branch_id") private Branch branch;
    @Column(name="customer_number") private String customerNumber;
    @Column(name="created_at") private LocalDateTime createdAt;
}
