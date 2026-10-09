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

public class PaymentDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentRequestDto {
        @NotBlank(message = "Payment type is required")
        private String paymentType; // One Time, Recurring

        @NotNull(message = "Amount is required")
        private BigDecimal amount;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paymentDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate nextPaymentDate;

        private String billingPeriod; // Monthly, Yearly
        private String status; // Pending, Received
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentResponseDto {
        private Long id;
        private Long projectId;
        private String paymentType;
        private BigDecimal amount;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate paymentDate;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate nextPaymentDate;

        private String billingPeriod;
        private String status;
        private String notes;
    }
}
