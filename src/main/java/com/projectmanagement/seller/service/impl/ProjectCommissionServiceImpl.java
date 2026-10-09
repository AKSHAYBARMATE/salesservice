package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.CalculateCommissionRequestDto;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectCommissionResponseDto;
import com.projectmanagement.seller.entity.Project;
import com.projectmanagement.seller.entity.ProjectCommission;
import com.projectmanagement.seller.entity.SalesLevel;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectCommissionRepository;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.ProjectCommissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectCommissionServiceImpl implements ProjectCommissionService {

    private final ProjectCommissionRepository commissionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectCommissionResponseDto>> getCommissionsByProject(Long projectId) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<ProjectCommissionResponseDto> list = commissionRepository.findByProjectId(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Project commission details fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectCommissionResponseDto>> getUserCommissions(Long userId) {
        Long targetUserId = (userId != null) ? userId : loginUser.getUserId();
        if (targetUserId == null) {
            return StandardResponse.success(List.of(), "No user context provided");
        }

        List<ProjectCommissionResponseDto> list = commissionRepository.findByUserId(targetUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "User commission earnings fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectCommissionResponseDto> calculateAndSaveCommission(Long projectId, CalculateCommissionRequestDto requestDto) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        Long userId = (requestDto != null && requestDto.getUserId() != null) ? requestDto.getUserId() :
                (project.getAssignedTo() != null ? project.getAssignedTo().getId() : null);

        if (userId == null) {
            throw new CustomException("No user specified or assigned to project for commission calculation", "USER_REQUIRED");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        SalesLevel level = user.getSalesLevel();
        BigDecimal commRate = level != null && level.getCommissionRate() != null ? level.getCommissionRate() : BigDecimal.ZERO;

        BigDecimal baseVal = (requestDto != null && requestDto.getExpectedValue() != null) ? requestDto.getExpectedValue() :
                (project.getExpectedValue() != null ? project.getExpectedValue() : BigDecimal.ZERO);

        if (baseVal.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Project base value cannot be negative", "INVALID_VALUE");
        }

        BigDecimal commAmount = baseVal.multiply(commRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        ProjectCommission commission = commissionRepository.findByProjectIdAndUserId(projectId, userId)
                .orElseGet(() -> ProjectCommission.builder()
                        .project(project)
                        .user(user)
                        .salesLevel(level)
                        .build());

        commission.setCommissionRate(commRate);
        commission.setCommissionAmount(commAmount);
        commission.setStatus("Pending");

        ProjectCommission saved = commissionRepository.save(commission);

        auditLogService.log(
                loginUser.getUserId(),
                "CALCULATE_COMMISSION",
                "COMMISSION",
                saved.getId(),
                null,
                "Commission calculated: rate=" + commRate + "%, amount=$" + commAmount,
                null
        );

        return StandardResponse.success(mapToDto(saved), "Commission calculated and saved successfully");
    }

    private ProjectCommissionResponseDto mapToDto(ProjectCommission c) {
        return ProjectCommissionResponseDto.builder()
                .id(c.getId())
                .projectId(c.getProject().getId())
                .projectTitle(c.getProject().getTitle())
                .userId(c.getUser().getId())
                .userName(c.getUser().getName())
                .levelId(c.getSalesLevel() != null ? c.getSalesLevel().getId() : null)
                .levelName(c.getSalesLevel() != null ? c.getSalesLevel().getLevelName() : null)
                .commissionRate(c.getCommissionRate())
                .commissionAmount(c.getCommissionAmount())
                .status(c.getStatus())
                .approvedById(c.getApprovedBy() != null ? c.getApprovedBy().getId() : null)
                .approvedByName(c.getApprovedBy() != null ? c.getApprovedBy().getName() : null)
                .approvedAt(c.getApprovedAt())
                .paidAt(c.getPaidAt())
                .build();
    }
}
