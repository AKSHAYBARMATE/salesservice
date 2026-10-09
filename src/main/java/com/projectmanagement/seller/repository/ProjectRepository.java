package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByClientId(Long clientId);
    List<Project> findByAssignedToId(Long assignedToId);

    @Query("SELECT p FROM Project p WHERE " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.client.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(p.client.companyName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:clientId IS NULL OR p.client.id = :clientId) AND " +
           "(:assignedTo IS NULL OR p.assignedTo.id = :assignedTo) AND " +
           "(:statusId IS NULL OR p.status.id = :statusId) AND " +
           "(p.isDeleted = false)")
    Page<Project> findWithFilters(@Param("search") String search,
                                  @Param("clientId") Long clientId,
                                  @Param("assignedTo") Long assignedTo,
                                  @Param("statusId") Long statusId,
                                  Pageable pageable);
}
