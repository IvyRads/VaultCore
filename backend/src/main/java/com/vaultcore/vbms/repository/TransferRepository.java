package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TransferRepository extends JpaRepository<Transfer,Integer> { List<Transfer> findBySenderAccountAccountIdOrReceiverAccountAccountIdOrderByTransferDateDesc(Integer sender, Integer receiver); }
