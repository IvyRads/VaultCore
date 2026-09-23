package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer,Integer> { Optional<Customer> findByUserUserId(Integer userId); Optional<Customer> findByCustomerNumber(String number); }
