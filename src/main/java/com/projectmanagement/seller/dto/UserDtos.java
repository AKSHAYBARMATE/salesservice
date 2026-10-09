package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserCreateRequestDto {
        @NotBlank(message = "User name is required")
        private String name;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        private String phone;
        private String password;
        private Long roleId;
        private Long levelId;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserUpdateRequestDto {
        private String name;
        private String phone;
        private Long roleId;
        private Long levelId;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatusUpdateRequestDto {
        @NotBlank(message = "Status is required")
        private String status; // ACTIVE, INACTIVE, SUSPENDED
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponseDto {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private Long roleId;
        private String roleName;
        private Long levelId;
        private String levelName;
        private Integer totalProjects;
        private BigDecimal totalCommission;
        private BigDecimal totalRoyalty;
        private String status;
    }
}
