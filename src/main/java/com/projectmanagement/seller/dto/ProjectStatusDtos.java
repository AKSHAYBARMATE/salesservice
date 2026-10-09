package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ProjectStatusDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStatusRequestDto {
        @NotBlank(message = "Status name is required")
        private String name;
        private String description;
        private Boolean isWonStatus;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectStatusResponseDto {
        private Long id;
        private String name;
        private String description;
        private Boolean isWonStatus;
        private Boolean isActive;
    }
}
