package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.QuestionDtos.*;
import com.projectmanagement.seller.service.ProjectQuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/questions")
@RequiredArgsConstructor
public class ProjectQuestionController {

    private final ProjectQuestionService questionService;

    @PostMapping("/createQuestionTemplate")
    public ResponseEntity<StandardResponse<QuestionTemplateResponseDto>> createQuestionTemplate(@Valid @RequestBody QuestionTemplateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(questionService.createQuestionTemplate(requestDto));
    }

    @GetMapping("/getAllQuestionTemplates")
    public ResponseEntity<StandardResponse<List<QuestionTemplateResponseDto>>> getAllQuestionTemplates() {
        return ResponseEntity.ok(questionService.getQuestionTemplates());
    }

    @PutMapping("/updateQuestionTemplate/{id}")
    public ResponseEntity<StandardResponse<QuestionTemplateResponseDto>> updateQuestionTemplate(
            @PathVariable Long id,
            @Valid @RequestBody QuestionTemplateRequestDto requestDto
    ) {
        return ResponseEntity.ok(questionService.updateQuestionTemplate(id, requestDto));
    }

    @GetMapping("/getProjectQuestions/{projectId}")
    public ResponseEntity<StandardResponse<List<QuestionTemplateResponseDto>>> getProjectQuestions(@PathVariable Long projectId) {
        return ResponseEntity.ok(questionService.getProjectQuestions(projectId));
    }

    @PutMapping("/saveProjectAnswers/{projectId}")
    public ResponseEntity<StandardResponse<List<ProjectAnswerResponseDto>>> saveProjectAnswers(
            @PathVariable Long projectId,
            @Valid @RequestBody SaveAnswersRequestDto requestDto
    ) {
        return ResponseEntity.ok(questionService.saveProjectAnswers(projectId, requestDto));
    }
}
