package com.projectmanagement.seller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProposalDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalRequestDto {
        @NotBlank(message = "Proposal name is required")
        private String proposalName;

        private String content;
        private BigDecimal oneTimePrice;
        private BigDecimal recurringAmount;
        private String billingFrequency; // Monthly, Yearly

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate validityDate;

        private Integer version;
        private String status;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate sentDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposalResponseDto {
        private Long id;
        private Long projectId;
        private String proposalName;
        private String content;
        private BigDecimal oneTimePrice;
        private BigDecimal recurringAmount;
        private String billingFrequency;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate validityDate;

        private Integer version;
        private String status;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate sentDate;

        private Long approvedById;
        private String approvedByName;
        private LocalDateTime approvedAt;
    }
}
