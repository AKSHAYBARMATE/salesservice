package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectQuestionRepository extends JpaRepository<ProjectQuestion, Long> {
    List<ProjectQuestion> findByIsActiveTrueOrderBySortOrderAsc();
}
