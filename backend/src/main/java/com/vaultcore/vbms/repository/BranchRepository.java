package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch,Integer> {
    @Query("""
            select b from Branch b
            where (:branchName is null or lower(coalesce(b.branchName, '')) like lower(concat('%', :branchName, '%')))
              and (:district is null or lower(coalesce(b.district, '')) like lower(concat('%', :district, '%')))
              and (:ifscCode is null or lower(coalesce(b.ifscCode, '')) like lower(concat('%', :ifscCode, '%')))
            order by b.branchName
            """)
    List<Branch> searchBranches(
            @Param("branchName") String branchName,
            @Param("district") String district,
            @Param("ifscCode") String ifscCode);
}
