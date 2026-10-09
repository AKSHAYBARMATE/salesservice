package com.projectmanagement.seller.repository;

import com.projectmanagement.seller.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {
    Optional<PasswordResetOtp> findTopByEmailAndOtpAndIsUsedFalseAndExpiresAtAfterOrderByCreatedOnDesc(
            String email,
            String otp,
            LocalDateTime now
    );
}
