package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectQuestionAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectQuestionAnswerRepository extends JpaRepository<ProjectQuestionAnswer, Long> {
    List<ProjectQuestionAnswer> findByProjectId(Long projectId);
    Optional<ProjectQuestionAnswer> findByProjectIdAndQuestionId(Long projectId, Long questionId);
}
