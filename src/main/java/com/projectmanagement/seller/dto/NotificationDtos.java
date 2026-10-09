package com.projectmanagement.seller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponseDto {
        private Long id;
        private Long userId;
        private String title;
        private String message;
        private Boolean isRead;
        private LocalDateTime createdOn;
    }
}
