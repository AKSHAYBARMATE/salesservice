package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Query("SELECT c FROM Client c WHERE " +
           "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(c.contactPerson) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR LOWER(c.status) = LOWER(:status)) AND " +
           "(c.isDeleted = false)")
    Page<Client> findWithFilters(@Param("search") String search,
                                 @Param("status") String status,
                                 Pageable pageable);
}
