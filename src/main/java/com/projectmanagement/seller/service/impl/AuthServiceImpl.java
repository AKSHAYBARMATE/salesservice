package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.JwtTokenProvider;
import com.projectmanagement.seller.dto.AuthDtos.*;
import com.projectmanagement.seller.entity.PasswordResetOtp;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.entity.UserSession;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.repository.PasswordResetOtpRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.repository.UserSessionRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.AuthService;
import com.projectmanagement.seller.util.EmailUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final PasswordResetOtpRepository passwordResetOtpRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailUtil emailUtil;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    public StandardResponse<LoginResponseDto> login(LoginRequestDto requestDto, HttpServletRequest request) {
        if (requestDto == null) {
            throw new CustomException("Login request payload is required", "INVALID_INPUT");
        }

        String identifier = requestDto.getIdentifier();
        if (identifier.isBlank()) {
            throw new CustomException("Username or Email is required", "INVALID_INPUT");
        }

        if (requestDto.getPassword() == null || requestDto.getPassword().isBlank()) {
            throw new CustomException("Password is required", "INVALID_INPUT");
        }

        User user = userRepository.findByEmailIgnoreCase(identifier)
                .orElseThrow(() -> new CustomException("Invalid username/email or password", "BAD_CREDENTIALS"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new CustomException("Account has been deleted. Please contact administration.", "ACCOUNT_DELETED");
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new CustomException("Account is " + user.getStatus() + ". Please contact administration.", "ACCOUNT_INACTIVE");
        }

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPasswordHash())) {
            throw new CustomException("Invalid username/email or password", "BAD_CREDENTIALS");
        }

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        Long roleId = user.getRole() != null ? user.getRole().getId() : null;
        Long levelId = user.getSalesLevel() != null ? user.getSalesLevel().getId() : null;
        String levelName = user.getSalesLevel() != null ? user.getSalesLevel().getLevelName() : null;

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roleName,
                roleId,
                levelId
        );

        String ipAddress = null;
        String userAgent = null;
        if (request != null) {
            ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isBlank()) {
                ipAddress = request.getRemoteAddr();
            }
            userAgent = request.getHeader("User-Agent");
        }

        UserSession session = UserSession.builder()
                .user(user)
                .token(token)
                .ipAddress(ipAddress)
                .deviceInfo(userAgent)
                .isActive(true)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        userSessionRepository.save(session);

        auditLogService.log(
                user.getId(),
                "LOGIN",
                "USER",
                user.getId(),
                null,
                "User successfully logged in: " + user.getEmail(),
                ipAddress
        );

        LoginResponseDto responseDto = LoginResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(roleName)
                .roleId(roleId)
                .levelId(levelId)
                .levelName(levelName)
                .build();

        return StandardResponse.success(responseDto, "Login successful");
    }

    @Override
    @Transactional
    public StandardResponse<Void> forgotPassword(ForgotPasswordRequestDto requestDto) {
        if (requestDto == null || requestDto.getEmail() == null || requestDto.getEmail().trim().isEmpty()) {
            throw new CustomException("Email is required", "INVALID_INPUT");
        }

        String email = requestDto.getEmail().trim();
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException("No account found with provided email: " + email, "USER_NOT_FOUND"));

        if (Boolean.TRUE.equals(user.getIsDeleted()) || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new CustomException("Account is " + user.getStatus() + ". Cannot reset password.", "ACCOUNT_INACTIVE");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1000000));

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .isUsed(false)
                .build();

        passwordResetOtpRepository.save(resetOtp);

        // Send OTP email
        emailUtil.sendPasswordResetOtpMail(user.getEmail(), user.getName(), otp);

        auditLogService.log(
                user.getId(),
                "FORGOT_PASSWORD_REQUEST",
                "USER",
                user.getId(),
                null,
                "Password reset OTP sent to: " + user.getEmail(),
                null
        );

        return StandardResponse.success("Password reset OTP has been sent successfully to " + user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Void> verifyOtp(VerifyOtpRequestDto requestDto) {
        if (requestDto == null || requestDto.getEmail() == null || requestDto.getOtp() == null) {
            throw new CustomException("Email and OTP are required", "INVALID_INPUT");
        }

        String email = requestDto.getEmail().trim();
        String otp = requestDto.getOtp().trim();

        passwordResetOtpRepository.findTopByEmailAndOtpAndIsUsedFalseAndExpiresAtAfterOrderByCreatedOnDesc(
                email,
                otp,
                LocalDateTime.now()
        ).orElseThrow(() -> new CustomException("Invalid or expired OTP. Please request a new OTP.", "INVALID_OTP"));

        return StandardResponse.success("OTP verified successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> resetPassword(ResetPasswordRequestDto requestDto) {
        if (requestDto == null || requestDto.getEmail() == null || requestDto.getOtp() == null || requestDto.getNewPassword() == null) {
            throw new CustomException("Email, OTP and new password are required", "INVALID_INPUT");
        }

        String email = requestDto.getEmail().trim();
        String otp = requestDto.getOtp().trim();
        String newPassword = requestDto.getNewPassword();

        if (newPassword.length() < 4) {
            throw new CustomException("Password must be at least 4 characters long", "INVALID_PASSWORD");
        }

        PasswordResetOtp resetOtp = passwordResetOtpRepository.findTopByEmailAndOtpAndIsUsedFalseAndExpiresAtAfterOrderByCreatedOnDesc(
                email,
                otp,
                LocalDateTime.now()
        ).orElseThrow(() -> new CustomException("Invalid or expired OTP. Please request a new OTP.", "INVALID_OTP"));

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new CustomException("User not found with email: " + email, "USER_NOT_FOUND"));

        // Invalidate OTP
        resetOtp.setIsUsed(true);
        passwordResetOtpRepository.save(resetOtp);

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Deactivate all active sessions for this user
        List<UserSession> sessions = userSessionRepository.findByUserIdAndIsActiveTrue(user.getId());
        for (UserSession s : sessions) {
            s.setIsActive(false);
            userSessionRepository.save(s);
        }

        auditLogService.log(
                user.getId(),
                "RESET_PASSWORD",
                "USER",
                user.getId(),
                null,
                "Password reset successfully for user: " + user.getEmail(),
                null
        );

        return StandardResponse.success("Password has been reset successfully. Please login with your new password.");
    }
}
