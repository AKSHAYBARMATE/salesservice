package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectRoyaltyResponseDto;
import com.projectmanagement.seller.entity.ProjectRoyalty;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.repository.ProjectRoyaltyRepository;
import com.projectmanagement.seller.service.ProjectRoyaltyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectRoyaltyServiceImpl implements ProjectRoyaltyService {

    private final ProjectRoyaltyRepository royaltyRepository;
    private final ProjectRepository projectRepository;
    private final LoginUser loginUser;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectRoyaltyResponseDto>> getRoyaltiesByProject(Long projectId) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<ProjectRoyaltyResponseDto> list = royaltyRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Project royalty details fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectRoyaltyResponseDto>> getUserRoyalties(Long userId) {
        Long targetUserId = (userId != null) ? userId : loginUser.getUserId();
        if (targetUserId == null) {
            return StandardResponse.success(List.of(), "No user context provided");
        }

        List<ProjectRoyaltyResponseDto> list = royaltyRepository.findByUserId(targetUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "User royalty earnings fetched successfully");
    }

    private ProjectRoyaltyResponseDto mapToDto(ProjectRoyalty r) {
        return ProjectRoyaltyResponseDto.builder()
                .id(r.getId())
                .projectId(r.getProject().getId())
                .projectTitle(r.getProject().getTitle())
                .userId(r.getUser().getId())
                .userName(r.getUser().getName())
                .levelId(r.getSalesLevel() != null ? r.getSalesLevel().getId() : null)
                .levelName(r.getSalesLevel() != null ? r.getSalesLevel().getLevelName() : null)
                .royaltyRate(r.getRoyaltyRate())
                .royaltyAmount(r.getRoyaltyAmount())
                .periodType(r.getPeriodType())
                .periodStart(r.getPeriodStart())
                .periodEnd(r.getPeriodEnd())
                .status(r.getStatus())
                .paidAt(r.getPaidAt())
                .build();
    }
}
