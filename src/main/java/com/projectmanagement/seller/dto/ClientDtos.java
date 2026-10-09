package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class ClientDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientRequestDto {
        @NotBlank(message = "Client name is required")
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String companyName;
        private String address;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientResponseDto {
        private Long id;
        private String name;
        private String contactPerson;
        private String email;
        private String phone;
        private String companyName;
        private String address;
        private String status;
    }
}
