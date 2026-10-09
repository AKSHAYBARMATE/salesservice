package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class AuthDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequestDto {
        private String username;
        private String email;

        @NotBlank(message = "Password is required")
        private String password;

        public String getIdentifier() {
            if (username != null && !username.isBlank()) {
                return username.trim();
            }
            if (email != null && !email.isBlank()) {
                return email.trim();
            }
            return "";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResponseDto {
        private String token;
        private Long userId;
        private String name;
        private String email;
        private String role;
        private Long roleId;
        private Long levelId;
        private String levelName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForgotPasswordRequestDto {
        @NotBlank(message = "Email or Username is required")
        private String email;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerifyOtpRequestDto {
        @NotBlank(message = "Email or Username is required")
        private String email;

        @NotBlank(message = "OTP is required")
        private String otp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResetPasswordRequestDto {
        @NotBlank(message = "Email or Username is required")
        private String email;

        @NotBlank(message = "OTP is required")
        private String otp;

        @NotBlank(message = "New password is required")
        private String newPassword;
    }
}
