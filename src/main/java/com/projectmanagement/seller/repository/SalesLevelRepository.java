package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.SalesLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalesLevelRepository extends JpaRepository<SalesLevel, Long> {
    Optional<SalesLevel> findByLevelNameIgnoreCase(String levelName);
    boolean existsByLevelNameIgnoreCase(String levelName);
    List<SalesLevel> findByIsActiveTrueOrderByMinProjectsAsc();
}
