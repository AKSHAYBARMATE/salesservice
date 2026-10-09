package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectRoyalty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRoyaltyRepository extends JpaRepository<ProjectRoyalty, Long> {
    List<ProjectRoyalty> findByProjectId(Long projectId);
    List<ProjectRoyalty> findByUserId(Long userId);
}
