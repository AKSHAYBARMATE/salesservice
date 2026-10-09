package com.projectmanagement.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class AuditLogDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuditLogResponseDto {
        private Long id;
        private Long userId;
        private String userName;
        private String action;
        private String entityType;
        private Long entityId;
        private String oldValue;
        private String newValue;
        private String ipAddress;
        private LocalDateTime createdOn;
    }
}
