package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name="branches") @Getter @Setter @NoArgsConstructor
public class Branch {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="branch_id") private Integer branchId;
    @Column(name="branch_name") private String branchName;
    private String district;
    private String address;
    @Column(name="ifsc_code") private String ifscCode;
    private String phone;
}
