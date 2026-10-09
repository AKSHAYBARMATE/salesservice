package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusRequestDto;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusResponseDto;
import com.projectmanagement.seller.entity.ProjectStatus;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectStatusRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.ProjectStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStatusServiceImpl implements ProjectStatusService {

    private final ProjectStatusRepository projectStatusRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<ProjectStatusResponseDto> createStatus(ProjectStatusRequestDto requestDto) {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Status name is required", "INVALID_INPUT");
        }

        String name = requestDto.getName().trim();
        if (projectStatusRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new CustomException("Project status already exists with name: " + name, "DUPLICATE_STATUS");
        }

        ProjectStatus status = ProjectStatus.builder()
                .name(name)
                .description(requestDto.getDescription())
                .isWonStatus(requestDto.getIsWonStatus() != null ? requestDto.getIsWonStatus() : false)
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        ProjectStatus saved = projectStatusRepository.save(status);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_STATUS",
                "PROJECT_STATUS",
                saved.getId(),
                null,
                "Created status: " + saved.getName(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Project status created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectStatusResponseDto>> getAllStatuses() {
        List<ProjectStatusResponseDto> list = projectStatusRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return StandardResponse.success(list, "Project statuses fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ProjectStatusResponseDto> getStatusById(Long id) {
        if (id == null) {
            throw new CustomException("Status ID cannot be null", "INVALID_INPUT");
        }
        ProjectStatus status = projectStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project status not found with id: " + id));
        return StandardResponse.success(mapToDto(status), "Project status fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectStatusResponseDto> updateStatus(Long id, ProjectStatusRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Status ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Status name is required for update", "INVALID_INPUT");
        }

        ProjectStatus status = projectStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project status not found with id: " + id));

        String newName = requestDto.getName().trim();
        if (!status.getName().equalsIgnoreCase(newName) && projectStatusRepository.findByNameIgnoreCase(newName).isPresent()) {
            throw new CustomException("Another status with name '" + newName + "' already exists", "DUPLICATE_STATUS");
        }

        status.setName(newName);
        if (requestDto.getDescription() != null) status.setDescription(requestDto.getDescription());
        if (requestDto.getIsWonStatus() != null) status.setIsWonStatus(requestDto.getIsWonStatus());
        if (requestDto.getIsActive() != null) status.setIsActive(requestDto.getIsActive());

        ProjectStatus updated = projectStatusRepository.save(status);

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_STATUS",
                "PROJECT_STATUS",
                updated.getId(),
                null,
                "Updated status: " + updated.getName(),
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project status updated successfully");
    }

    private ProjectStatusResponseDto mapToDto(ProjectStatus status) {
        return ProjectStatusResponseDto.builder()
                .id(status.getId())
                .name(status.getName())
                .description(status.getDescription())
                .isWonStatus(status.getIsWonStatus())
                .isActive(status.getIsActive())
                .build();
    }
}
