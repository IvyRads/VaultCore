package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BranchRepository extends JpaRepository<Branch,Integer> {
    default List<Branch> searchBranches(String branchName, String district, String ifscCode) {
        return searchBranchesFiltered(normalize(branchName), normalize(district), normalize(ifscCode));
    }

    @Query("""
            select b from Branch b
            where (:branchName = '' or lower(coalesce(b.branchName, '')) like lower(concat('%', :branchName, '%')))
              and (:district = '' or lower(coalesce(b.district, '')) like lower(concat('%', :district, '%')))
              and (:ifscCode = '' or lower(coalesce(b.ifscCode, '')) like lower(concat('%', :ifscCode, '%')))
            order by b.branchName
            """)
    List<Branch> searchBranchesFiltered(
            @Param("branchName") String branchName,
            @Param("district") String district,
            @Param("ifscCode") String ifscCode);

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "" : value.trim();
    }
}
