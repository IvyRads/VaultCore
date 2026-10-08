package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface SupportTicketRepository extends JpaRepository<SupportTicket,Integer> {
    List<SupportTicket> findByCustomerCustomerIdOrderByCreatedAtDesc(Integer customerId);
    @Query("select t from SupportTicket t join fetch t.customer where t.ticketId = :ticketId")
    Optional<SupportTicket> findWithCustomerByTicketId(@Param("ticketId") int ticketId);
}
