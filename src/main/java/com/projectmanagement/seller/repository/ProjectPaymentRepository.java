package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.ProjectPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectPaymentRepository extends JpaRepository<ProjectPayment, Long> {
    List<ProjectPayment> findByProjectIdOrderByPaymentDateDesc(Long projectId);
}
