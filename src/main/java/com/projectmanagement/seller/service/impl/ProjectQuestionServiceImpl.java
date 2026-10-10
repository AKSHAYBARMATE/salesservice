package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.QuestionDtos.*;
import com.projectmanagement.seller.entity.Project;
import com.projectmanagement.seller.entity.ProjectQuestion;
import com.projectmanagement.seller.entity.ProjectQuestionAnswer;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectQuestionAnswerRepository;
import com.projectmanagement.seller.repository.ProjectQuestionRepository;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.ProjectQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectQuestionServiceImpl implements ProjectQuestionService {

    private final ProjectQuestionRepository questionRepository;
    private final ProjectQuestionAnswerRepository answerRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<QuestionTemplateResponseDto> createQuestionTemplate(QuestionTemplateRequestDto requestDto) {
        if (requestDto == null || requestDto.getQuestionText() == null || requestDto.getQuestionText().trim().isEmpty()) {
            throw new CustomException("Question text is required", "INVALID_INPUT");
        }
        if (requestDto.getQuestionType() == null || requestDto.getQuestionType().trim().isEmpty()) {
            throw new CustomException("Question type is required", "INVALID_INPUT");
        }

        ProjectQuestion question = ProjectQuestion.builder()
                .questionText(requestDto.getQuestionText().trim())
                .questionType(requestDto.getQuestionType().trim())
                .options(requestDto.getOptions())
                .isRequired(requestDto.getIsRequired() != null ? requestDto.getIsRequired() : true)
                .sortOrder(requestDto.getSortOrder() != null ? requestDto.getSortOrder() : 0)
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        ProjectQuestion saved = questionRepository.save(question);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_QUESTION_TEMPLATE",
                "QUESTION_TEMPLATE",
                saved.getId(),
                null,
                "Created question: " + saved.getQuestionText(),
                null
        );

        return StandardResponse.success(mapQuestionToDto(saved), "Question template created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<QuestionTemplateResponseDto>> getQuestionTemplates() {
        List<QuestionTemplateResponseDto> list = questionRepository.findByIsActiveTrueOrderBySortOrderAsc().stream()
                .map(this::mapQuestionToDto)
                .collect(Collectors.toList());
        return StandardResponse.success(list, "Question templates fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<QuestionTemplateResponseDto> updateQuestionTemplate(Long id, QuestionTemplateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Question ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null) {
            throw new CustomException("Update payload is required", "INVALID_INPUT");
        }

        ProjectQuestion question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question template not found with id: " + id));

        if (requestDto.getQuestionText() != null && !requestDto.getQuestionText().trim().isEmpty()) {
            question.setQuestionText(requestDto.getQuestionText().trim());
        }
        if (requestDto.getQuestionType() != null && !requestDto.getQuestionType().trim().isEmpty()) {
            question.setQuestionType(requestDto.getQuestionType().trim());
        }
        if (requestDto.getOptions() != null) question.setOptions(requestDto.getOptions());
        if (requestDto.getIsRequired() != null) question.setIsRequired(requestDto.getIsRequired());
        if (requestDto.getSortOrder() != null) question.setSortOrder(requestDto.getSortOrder());
        if (requestDto.getIsActive() != null) question.setIsActive(requestDto.getIsActive());

        ProjectQuestion updated = questionRepository.save(question);

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_QUESTION_TEMPLATE",
                "QUESTION_TEMPLATE",
                updated.getId(),
                null,
                "Updated question template",
                null
        );

        return StandardResponse.success(mapQuestionToDto(updated), "Question template updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<QuestionTemplateResponseDto>> getProjectQuestions(Long projectId) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<ProjectQuestion> questions = questionRepository.findByIsActiveTrueOrderBySortOrderAsc();
        List<ProjectQuestionAnswer> answers = answerRepository.findByProjectId(projectId);

        Map<Long, ProjectQuestionAnswer> answerMap = answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.toMap(
                        a -> a.getQuestion().getId(),
                        a -> a,
                        (existing, replacement) -> replacement
                ));

        List<QuestionTemplateResponseDto> result = questions.stream()
                .map(q -> {
                    ProjectQuestionAnswer a = answerMap.get(q.getId());
                    return QuestionTemplateResponseDto.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .questionType(q.getQuestionType())
                            .options(q.getOptions())
                            .isRequired(q.getIsRequired())
                            .sortOrder(q.getSortOrder())
                            .isActive(q.getIsActive())
                            .answerId(a != null ? a.getId() : null)
                            .answerText(a != null ? a.getAnswerText() : null)
                            .answerOption(a != null ? a.getAnswerOption() : null)
                            .answeredById(a != null && a.getAnsweredBy() != null ? a.getAnsweredBy().getId() : null)
                            .answeredByName(a != null && a.getAnsweredBy() != null ? a.getAnsweredBy().getName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return StandardResponse.success(result, "Project questions and answers fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<List<ProjectAnswerResponseDto>> saveProjectAnswers(Long projectId, SaveAnswersRequestDto requestDto) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (requestDto == null || requestDto.getAnswers() == null || requestDto.getAnswers().isEmpty()) {
            throw new CustomException("At least one answer must be provided", "INVALID_INPUT");
        }

        User answeredBy = null;
        if (loginUser.getUserId() != null) {
            answeredBy = userRepository.findById(loginUser.getUserId()).orElse(null);
        }

        List<ProjectQuestionAnswer> savedAnswers = new ArrayList<>();

        for (AnswerItemDto item : requestDto.getAnswers()) {
            if (item.getQuestionId() == null) {
                throw new CustomException("Question ID is required for answer", "INVALID_INPUT");
            }

            ProjectQuestion question = questionRepository.findById(item.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + item.getQuestionId()));

            ProjectQuestionAnswer answer = answerRepository.findByProjectIdAndQuestionId(projectId, item.getQuestionId())
                    .orElseGet(() -> ProjectQuestionAnswer.builder()
                            .project(project)
                            .question(question)
                            .build());

            answer.setAnswerText(item.getAnswerText());
            answer.setAnswerOption(item.getAnswerOption());
            answer.setAnsweredBy(answeredBy);

            savedAnswers.add(answerRepository.save(answer));
        }

        auditLogService.log(
                loginUser.getUserId(),
                "SAVE_PROJECT_ANSWERS",
                "PROJECT",
                projectId,
                null,
                "Saved " + savedAnswers.size() + " answers for project",
                null
        );

        List<ProjectAnswerResponseDto> dtos = savedAnswers.stream()
                .map(this::mapAnswerToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(dtos, "Project answers saved successfully");
    }

    private QuestionTemplateResponseDto mapQuestionToDto(ProjectQuestion q) {
        return QuestionTemplateResponseDto.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionType(q.getQuestionType())
                .options(q.getOptions())
                .isRequired(q.getIsRequired())
                .sortOrder(q.getSortOrder())
                .isActive(q.getIsActive())
                .build();
    }

    private ProjectAnswerResponseDto mapAnswerToDto(ProjectQuestionAnswer a) {
        return ProjectAnswerResponseDto.builder()
                .id(a.getId())
                .projectId(a.getProject().getId())
                .questionId(a.getQuestion().getId())
                .questionText(a.getQuestion().getQuestionText())
                .questionType(a.getQuestion().getQuestionType())
                .answerText(a.getAnswerText())
                .answerOption(a.getAnswerOption())
                .answeredById(a.getAnsweredBy() != null ? a.getAnsweredBy().getId() : null)
                .answeredByName(a.getAnsweredBy() != null ? a.getAnsweredBy().getName() : null)
                .build();
    }
}
