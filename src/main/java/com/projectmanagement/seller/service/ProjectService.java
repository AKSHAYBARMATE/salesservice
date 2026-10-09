package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProjectService {
    StandardResponse<ProjectResponseDto> createProject(ProjectCreateRequestDto requestDto);
    StandardResponse<Page<ProjectResponseDto>> getProjects(String search, Long clientId, Long assignedTo, Long statusId, Pageable pageable);
    StandardResponse<ProjectResponseDto> getProjectById(Long id);
    StandardResponse<ProjectResponseDto> updateProject(Long id, ProjectUpdateRequestDto requestDto);
    StandardResponse<ProjectResponseDto> updateProjectStatus(Long id, ProjectStatusUpdateRequestDto requestDto);
    StandardResponse<ProjectResponseDto> submitForReview(Long id);
    StandardResponse<ProjectResponseDto> adminApprove(Long id);
    StandardResponse<ProjectResponseDto> markWon(Long id);
    StandardResponse<ProjectResponseDto> markLost(Long id);
    StandardResponse<List<AuditLogResponseDto>> getProjectTimeline(Long id);
}
