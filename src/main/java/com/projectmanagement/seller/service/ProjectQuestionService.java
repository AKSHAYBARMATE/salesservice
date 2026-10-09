package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.QuestionDtos.*;

import java.util.List;

public interface ProjectQuestionService {
    StandardResponse<QuestionTemplateResponseDto> createQuestionTemplate(QuestionTemplateRequestDto requestDto);
    StandardResponse<List<QuestionTemplateResponseDto>> getQuestionTemplates();
    StandardResponse<QuestionTemplateResponseDto> updateQuestionTemplate(Long id, QuestionTemplateRequestDto requestDto);
    StandardResponse<List<QuestionTemplateResponseDto>> getProjectQuestions(Long projectId);
    StandardResponse<List<ProjectAnswerResponseDto>> saveProjectAnswers(Long projectId, SaveAnswersRequestDto requestDto);
    StandardResponse<List<ProjectAnswerResponseDto>> getProjectAnswers(Long projectId);
}
