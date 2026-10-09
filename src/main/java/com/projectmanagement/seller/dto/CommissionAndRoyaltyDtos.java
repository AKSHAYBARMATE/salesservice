package com.projectmanagement.seller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CommissionAndRoyaltyDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCommissionResponseDto {
        private Long id;
        private Long projectId;
        private String projectTitle;
        private Long userId;
        private String userName;
        private Long levelId;
        private String levelName;
        private BigDecimal commissionRate;
        private BigDecimal commissionAmount;
        private String status;
        private Long approvedById;
        private String approvedByName;
        private LocalDateTime approvedAt;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectRoyaltyResponseDto {
        private Long id;
        private Long projectId;
        private String projectTitle;
        private Long userId;
        private String userName;
        private Long levelId;
        private String levelName;
        private BigDecimal royaltyRate;
        private BigDecimal royaltyAmount;
        private String periodType;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate periodStart;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate periodEnd;

        private String status;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalculateCommissionRequestDto {
        private Long userId;
        private BigDecimal expectedValue;
    }
}
