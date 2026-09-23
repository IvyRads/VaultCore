package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Integer> { List<SupportTicket> findByCustomerCustomerIdOrderByCreatedAtDesc(Integer customerId); }
