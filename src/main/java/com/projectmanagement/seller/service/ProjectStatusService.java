package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusRequestDto;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusResponseDto;

import java.util.List;

public interface ProjectStatusService {
    StandardResponse<ProjectStatusResponseDto> createStatus(ProjectStatusRequestDto requestDto);
    StandardResponse<List<ProjectStatusResponseDto>> getAllStatuses();
    StandardResponse<ProjectStatusResponseDto> getStatusById(Long id);
    StandardResponse<ProjectStatusResponseDto> updateStatus(Long id, ProjectStatusRequestDto requestDto);
}
