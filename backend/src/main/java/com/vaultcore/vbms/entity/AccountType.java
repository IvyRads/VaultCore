package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity @Table(name="account_types") @Getter @Setter @NoArgsConstructor
public class AccountType {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="type_id") private Integer typeId;
    @Column(name="type_name") private String typeName;
    @Column(name="minimum_balance") private BigDecimal minimumBalance;
    @Column(name="interest_rate") private BigDecimal interestRate;
}
