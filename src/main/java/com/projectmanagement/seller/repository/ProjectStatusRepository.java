package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectStatusRepository extends JpaRepository<ProjectStatus, Long> {
    Optional<ProjectStatus> findByNameIgnoreCase(String name);
    List<ProjectStatus> findByIsActiveTrueOrderByIdAsc();
}
