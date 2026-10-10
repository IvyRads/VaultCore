package com.vaultcore.vbms.repository;
import com.vaultcore.vbms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface CustomerRepository extends JpaRepository<Customer,Integer> {
    Optional<Customer> findByUserUserId(Integer userId);
    Optional<Customer> findByCustomerNumber(String number);

    @Query("select c from Customer c where c.branch.branchId = :branchId order by c.customerId")
    List<Customer> findByBranchId(@Param("branchId") Integer branchId);

    @Query("select count(c) from Customer c where c.branch.branchId = :branchId")
    long countByBranchId(@Param("branchId") Integer branchId);

    default List<Customer> searchCustomers(String search, Integer branchId) {
        return searchCustomersFiltered(
                search == null || search.isBlank() ? "" : search.trim(),
                branchId == null ? -1 : branchId);
    }

    @Query("""
            select c from Customer c
            where (:branchId = -1 or c.branch.branchId = :branchId)
              and (
                    :search = ''
                    or lower(c.customerNumber) like lower(concat('%', :search, '%'))
                    or lower(c.user.email) like lower(concat('%', :search, '%'))
                    or exists (
                        select p.profileId from UserProfile p
                        where p.user.userId = c.user.userId
                          and (
                                lower(coalesce(p.firstName, '')) like lower(concat('%', :search, '%'))
                                or lower(coalesce(p.lastName, '')) like lower(concat('%', :search, '%'))
                                or lower(coalesce(p.phone, '')) like lower(concat('%', :search, '%'))
                          )
                    )
              )
            """)
    List<Customer> searchCustomersFiltered(@Param("search") String search, @Param("branchId") Integer branchId);
}
