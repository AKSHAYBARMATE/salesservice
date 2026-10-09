package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SalesLevelDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesLevelRequestDto {
        @NotBlank(message = "Level name is required")
        private String levelName;
        private Integer minProjects;
        private BigDecimal commissionRate;
        private BigDecimal royaltyRate;
        private String description;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesLevelResponseDto {
        private Long id;
        private String levelName;
        private Integer minProjects;
        private BigDecimal commissionRate;
        private BigDecimal royaltyRate;
        private String description;
        private Boolean isActive;
    }
}
