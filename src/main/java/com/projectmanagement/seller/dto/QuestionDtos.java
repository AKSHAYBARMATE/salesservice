package com.projectmanagement.seller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class QuestionDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTemplateRequestDto {
        @NotBlank(message = "Question text is required")
        private String questionText;

        @NotBlank(message = "Question type is required")
        private String questionType; // Text, Single Choice, Multiple Choice

        private String options; // JSON string or comma-separated
        private Boolean isRequired;
        private Integer sortOrder;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTemplateResponseDto {
        private Long id;
        private String questionText;
        private String questionType;
        private String options;
        private Boolean isRequired;
        private Integer sortOrder;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItemDto {
        @NotNull(message = "Question ID is required")
        private Long questionId;
        private String answerText;
        private String answerOption;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaveAnswersRequestDto {
        private List<AnswerItemDto> answers;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAnswerResponseDto {
        private Long id;
        private Long projectId;
        private Long questionId;
        private String questionText;
        private String questionType;
        private String answerText;
        private String answerOption;
        private Long answeredById;
        private String answeredByName;
    }
}
