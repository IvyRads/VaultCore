package com.vaultcore.vbms.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="support_tickets") @Getter @Setter @NoArgsConstructor
public class SupportTicket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ticket_id") private Integer ticketId;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="customer_id", nullable=false) private Customer customer;
    private String subject;
    private String issue;
    private String status;
    @Column(name="created_at") private LocalDateTime createdAt;
}
