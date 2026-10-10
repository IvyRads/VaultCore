package com.vaultcore.vbms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.vaultcore.vbms.entity.Card;

import jakarta.persistence.LockModeType;

public interface CardRepository extends JpaRepository<Card, Integer> {
    List<Card> findByAccountCustomerCustomerIdOrderByCardIdDesc(Integer customerId);
    long countByAccountAccountId(Integer accountId);
    boolean existsByCardNumber(String cardNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Card c join fetch c.account where c.cardId = :id")
    Optional<Card> findForUpdate(Integer id);
}
