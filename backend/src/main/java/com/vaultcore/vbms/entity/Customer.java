package com.vaultcore.vbms.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="customers") @Getter @Setter @NoArgsConstructor
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Integer customerId;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false) private User user;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="branch_id") private Branch branch;
    @Column(name="customer_number") private String customerNumber;
    @Column(name="created_at") private LocalDateTime createdAt;
}
