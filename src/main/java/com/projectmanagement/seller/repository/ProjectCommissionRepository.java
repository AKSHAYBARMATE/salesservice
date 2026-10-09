package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectCommission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectCommissionRepository extends JpaRepository<ProjectCommission, Long> {
    List<ProjectCommission> findByProjectId(Long projectId);
    List<ProjectCommission> findByUserId(Long userId);
    Optional<ProjectCommission> findByProjectIdAndUserId(Long projectId, Long userId);
}
