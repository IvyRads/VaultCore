package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserProfileRepository extends JpaRepository<UserProfile,Integer> { Optional<UserProfile> findByUserUserId(Integer userId); }
