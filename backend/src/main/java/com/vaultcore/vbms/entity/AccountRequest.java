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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account_requests")
@Getter @Setter @NoArgsConstructor
public class AccountRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String reason;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(nullable = false)
    private String status;

    public void setAccount(Account account) { this.account = account; }
    public void setAction(String action) { this.action = action; }
    public void setReason(String reason) { this.reason = reason; }
    public void setRequestDate(LocalDateTime requestDate) { this.requestDate = requestDate; }
    public void setStatus(String status) { this.status = status; }
}
