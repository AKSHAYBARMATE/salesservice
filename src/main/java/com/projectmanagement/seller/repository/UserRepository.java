package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    @Query("SELECT u FROM User u WHERE " +
           "(:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:roleId IS NULL OR u.role.id = :roleId) AND " +
           "(:levelId IS NULL OR u.salesLevel.id = :levelId) AND " +
           "(:status IS NULL OR LOWER(u.status) = LOWER(:status)) AND " +
           "(u.isDeleted = false)")
    Page<User> findWithFilters(@Param("search") String search,
                               @Param("roleId") Long roleId,
                               @Param("levelId") Long levelId,
                               @Param("status") String status,
                               Pageable pageable);
}
