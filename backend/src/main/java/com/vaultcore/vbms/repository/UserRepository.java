package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Integer> { Optional<User> findByEmailIgnoreCase(String email); boolean existsByEmailIgnoreCase(String email); }
