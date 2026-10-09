package com.projectmanagement.seller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCreateRequestDto {
        @NotBlank(message = "Title is required")
        private String title;

        private String description;

        @NotNull(message = "Client ID is required")
        private Long clientId;

        private Long assignedTo;

        private Long statusId;

        private BigDecimal expectedValue;
        private BigDecimal expectedRoyalty;
        private BigDecimal expectedCommission;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedCloseDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectUpdateRequestDto {
        private String title;
        private String description;
        private Long clientId;
        private Long assignedTo;
        private Long statusId;
        private BigDecimal expectedValue;
        private BigDecimal expectedRoyalty;
        private BigDecimal expectedCommission;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedCloseDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate actualCloseDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStatusUpdateRequestDto {
        @NotNull(message = "Status ID is required")
        private Long statusId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAssignSalesPersonRequestDto {
        @NotNull(message = "Salesperson ID is required")
        private Long salesPersonId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectResponseDto {
        private Long id;
        private String title;
        private String description;

        private Long clientId;
        private String clientName;
        private String companyName;

        private Long createdByUserId;
        private String createdByUserName;

        private Long assignedToUserId;
        private String assignedToUserName;

        private Long statusId;
        private String statusName;

        private BigDecimal expectedValue;
        private BigDecimal expectedRoyalty;
        private BigDecimal expectedCommission;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate expectedCloseDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate actualCloseDate;
    }
}
