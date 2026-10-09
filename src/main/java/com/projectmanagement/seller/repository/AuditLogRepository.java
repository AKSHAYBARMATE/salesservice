package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedOnDesc(String entityType, Long entityId);
    List<AuditLog> findByUserIdOrderByCreatedOnDesc(Long userId);

    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:entityType IS NULL OR LOWER(a.entityType) = LOWER(:entityType)) AND " +
           "(:entityId IS NULL OR a.entityId = :entityId) AND " +
           "(:action IS NULL OR LOWER(a.action) = LOWER(:action)) " +
           "ORDER BY a.createdOn DESC")
    Page<AuditLog> findWithFilters(@Param("userId") Long userId,
                                   @Param("entityType") String entityType,
                                   @Param("entityId") Long entityId,
                                   @Param("action") String action,
                                   Pageable pageable);
}
