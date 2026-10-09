package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectProposalRepository extends JpaRepository<ProjectProposal, Long> {
    List<ProjectProposal> findByProjectIdOrderByVersionDesc(Long projectId);
}
