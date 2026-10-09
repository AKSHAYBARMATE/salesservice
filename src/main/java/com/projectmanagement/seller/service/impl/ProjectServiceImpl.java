package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.*;
import com.projectmanagement.seller.entity.*;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.*;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.NotificationService;
import com.projectmanagement.seller.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final ProjectStatusRepository projectStatusRepository;
    private final ProjectCommissionRepository commissionRepository;
    private final ProjectRoyaltyRepository royaltyRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> createProject(ProjectCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new CustomException("Project create payload is required", "INVALID_INPUT");
        }
        if (requestDto.getTitle() == null || requestDto.getTitle().trim().isEmpty()) {
            throw new CustomException("Project title is required", "INVALID_INPUT");
        }
        if (requestDto.getClientId() == null) {
            throw new CustomException("Client ID is required", "INVALID_INPUT");
        }

        Client client = clientRepository.findById(requestDto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + requestDto.getClientId()));

        User currentUser = null;
        if (loginUser.getUserId() != null) {
            currentUser = userRepository.findById(loginUser.getUserId()).orElse(null);
        }

        User createdByUser = currentUser;
        User assignedTo = currentUser;

        ProjectStatus status = null;
        if (requestDto.getStatusId() != null) {
            status = projectStatusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project status not found with id: " + requestDto.getStatusId()));
        } else {
            status = projectStatusRepository.findByNameIgnoreCase("Lead")
                    .orElseGet(() -> projectStatusRepository.findAll().stream().findFirst().orElse(null));
        }

        BigDecimal expectedValue = requestDto.getExpectedValue() != null ? requestDto.getExpectedValue() : BigDecimal.ZERO;
        if (expectedValue.compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Expected value cannot be negative", "INVALID_VALUE");
        }

        BigDecimal expectedCommission = requestDto.getExpectedCommission();
        BigDecimal expectedRoyalty = requestDto.getExpectedRoyalty();

        // Auto calculate expected commission and royalty based on assigned user's sales level
        if (assignedTo != null && assignedTo.getSalesLevel() != null && expectedValue.compareTo(BigDecimal.ZERO) > 0) {
            SalesLevel level = assignedTo.getSalesLevel();
            if (expectedCommission == null && level.getCommissionRate() != null) {
                expectedCommission = expectedValue.multiply(level.getCommissionRate())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
            if (expectedRoyalty == null && level.getRoyaltyRate() != null) {
                expectedRoyalty = expectedValue.multiply(level.getRoyaltyRate())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }

        Project project = Project.builder()
                .title(requestDto.getTitle().trim())
                .description(requestDto.getDescription())
                .client(client)
                .createdByUser(createdByUser)
                .assignedTo(assignedTo)
                .status(status)
                .expectedValue(expectedValue)
                .expectedCommission(expectedCommission != null ? expectedCommission : BigDecimal.ZERO)
                .expectedRoyalty(expectedRoyalty != null ? expectedRoyalty : BigDecimal.ZERO)
                .startDate(requestDto.getStartDate())
                .expectedCloseDate(requestDto.getExpectedCloseDate())
                .build();

        Project saved = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_PROJECT",
                "PROJECT",
                saved.getId(),
                null,
                "Project created: " + saved.getTitle() + ", Client: " + client.getName(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Project created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<ProjectResponseDto>> getProjects(String search, Long clientId, Long assignedTo, Long statusId, Pageable pageable) {
        Page<ProjectResponseDto> page = projectRepository.findWithFilters(search, clientId, assignedTo, statusId, pageable)
                .map(this::mapToDto);

        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return StandardResponse.success(page, "Projects fetched successfully", metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ProjectResponseDto> getProjectById(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        return StandardResponse.success(mapToDto(project), "Project details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> updateProject(Long id, ProjectUpdateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null) {
            throw new CustomException("Update payload is required", "INVALID_INPUT");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        String oldVal = "title=" + project.getTitle() + ", expectedValue=" + project.getExpectedValue();

        if (requestDto.getTitle() != null && !requestDto.getTitle().trim().isEmpty()) {
            project.setTitle(requestDto.getTitle().trim());
        }
        if (requestDto.getDescription() != null) project.setDescription(requestDto.getDescription());

        if (requestDto.getClientId() != null) {
            Client client = clientRepository.findById(requestDto.getClientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + requestDto.getClientId()));
            project.setClient(client);
        }

        if (requestDto.getStatusId() != null) {
            ProjectStatus status = projectStatusRepository.findById(requestDto.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project status not found with id: " + requestDto.getStatusId()));
            project.setStatus(status);
        }

        if (requestDto.getExpectedValue() != null) {
            if (requestDto.getExpectedValue().compareTo(BigDecimal.ZERO) < 0) {
                throw new CustomException("Expected value cannot be negative", "INVALID_VALUE");
            }
            project.setExpectedValue(requestDto.getExpectedValue());
        }
        if (requestDto.getExpectedCommission() != null) project.setExpectedCommission(requestDto.getExpectedCommission());
        if (requestDto.getExpectedRoyalty() != null) project.setExpectedRoyalty(requestDto.getExpectedRoyalty());
        if (requestDto.getStartDate() != null) project.setStartDate(requestDto.getStartDate());
        if (requestDto.getExpectedCloseDate() != null) project.setExpectedCloseDate(requestDto.getExpectedCloseDate());
        if (requestDto.getActualCloseDate() != null) project.setActualCloseDate(requestDto.getActualCloseDate());

        Project updated = projectRepository.save(project);

        String newVal = "title=" + updated.getTitle() + ", expectedValue=" + updated.getExpectedValue();

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_PROJECT",
                "PROJECT",
                updated.getId(),
                oldVal,
                newVal,
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> updateProjectStatus(Long id, ProjectStatusUpdateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getStatusId() == null) {
            throw new CustomException("Status ID is required", "INVALID_INPUT");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        ProjectStatus newStatus = projectStatusRepository.findById(requestDto.getStatusId())
                .orElseThrow(() -> new ResourceNotFoundException("Project status not found with id: " + requestDto.getStatusId()));

        String oldStatus = project.getStatus() != null ? project.getStatus().getName() : "None";
        project.setStatus(newStatus);
        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "STATUS_CHANGE",
                "PROJECT",
                updated.getId(),
                "status=" + oldStatus,
                "status=" + newStatus.getName(),
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project status updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> submitForReview(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        ProjectStatus submittedStatus = projectStatusRepository.findByNameIgnoreCase("Submitted")
                .orElseGet(() -> projectStatusRepository.findByNameIgnoreCase("In Review")
                        .orElse(project.getStatus()));

        project.setStatus(submittedStatus);
        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "SUBMIT_FOR_REVIEW",
                "PROJECT",
                updated.getId(),
                null,
                "Project submitted for admin review",
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project submitted for review successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> adminApprove(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        ProjectStatus approvedStatus = projectStatusRepository.findByNameIgnoreCase("Approved")
                .orElseGet(() -> projectStatusRepository.findByNameIgnoreCase("Negotiation")
                        .orElse(project.getStatus()));

        project.setStatus(approvedStatus);
        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "ADMIN_APPROVE",
                "PROJECT",
                updated.getId(),
                null,
                "Project approved by admin",
                null
        );

        if (project.getAssignedTo() != null) {
            notificationService.sendNotification(
                    project.getAssignedTo().getId(),
                    "Project Approved",
                    "Project '" + project.getTitle() + "' has been approved by admin."
            );
        }

        return StandardResponse.success(mapToDto(updated), "Project approved by admin successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> markWon(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        if (project.getStatus() != null && "Won".equalsIgnoreCase(project.getStatus().getName())) {
            throw new CustomException("Project is already marked as WON", "ALREADY_WON");
        }

        ProjectStatus wonStatus = projectStatusRepository.findByNameIgnoreCase("Won")
                .orElseGet(() -> projectStatusRepository.findAll().stream()
                        .filter(s -> Boolean.TRUE.equals(s.getIsWonStatus()))
                        .findFirst()
                        .orElse(project.getStatus()));

        project.setStatus(wonStatus);
        project.setActualCloseDate(LocalDate.now());
        Project updated = projectRepository.save(project);

        // Record Commission and Royalty
        User assignedUser = project.getAssignedTo();
        if (assignedUser != null) {
            SalesLevel level = assignedUser.getSalesLevel();
            BigDecimal commRate = level != null && level.getCommissionRate() != null ? level.getCommissionRate() : BigDecimal.ZERO;
            BigDecimal commAmount = project.getExpectedValue() != null ?
                    project.getExpectedValue().multiply(commRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            BigDecimal royRate = level != null && level.getRoyaltyRate() != null ? level.getRoyaltyRate() : BigDecimal.ZERO;
            BigDecimal royAmount = project.getExpectedValue() != null ?
                    project.getExpectedValue().multiply(royRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

            ProjectCommission commission = ProjectCommission.builder()
                    .project(updated)
                    .user(assignedUser)
                    .salesLevel(level)
                    .commissionRate(commRate)
                    .commissionAmount(commAmount)
                    .status("Pending")
                    .build();
            commissionRepository.save(commission);

            ProjectRoyalty royalty = ProjectRoyalty.builder()
                    .project(updated)
                    .user(assignedUser)
                    .salesLevel(level)
                    .royaltyRate(royRate)
                    .royaltyAmount(royAmount)
                    .periodType("Monthly")
                    .periodStart(LocalDate.now())
                    .status("Pending")
                    .build();
            royaltyRepository.save(royalty);

            // Update user lifetime stats
            assignedUser.setTotalProjects(assignedUser.getTotalProjects() + 1);
            assignedUser.setTotalCommission(assignedUser.getTotalCommission().add(commAmount));
            assignedUser.setTotalRoyalty(assignedUser.getTotalRoyalty().add(royAmount));
            userRepository.save(assignedUser);

            notificationService.sendNotification(
                    assignedUser.getId(),
                    "Project Won!",
                    "Congratulations! Project '" + project.getTitle() + "' marked as WON. Expected commission: $" + commAmount
            );
        }

        auditLogService.log(
                loginUser.getUserId(),
                "MARK_WON",
                "PROJECT",
                updated.getId(),
                null,
                "Project marked as WON. Commission and Royalty recorded.",
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project marked as WON successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> markLost(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        ProjectStatus lostStatus = projectStatusRepository.findByNameIgnoreCase("Lost")
                .orElse(project.getStatus());

        project.setStatus(lostStatus);
        project.setActualCloseDate(LocalDate.now());
        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "MARK_LOST",
                "PROJECT",
                updated.getId(),
                null,
                "Project marked as LOST",
                null
        );

        if (project.getAssignedTo() != null) {
            notificationService.sendNotification(
                    project.getAssignedTo().getId(),
                    "Project Closed - Lost",
                    "Project '" + project.getTitle() + "' has been marked as LOST."
            );
        }

        return StandardResponse.success(mapToDto(updated), "Project marked as LOST successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<AuditLogResponseDto>> getProjectTimeline(Long id) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project not found with id: " + id);
        }
        return auditLogService.getAuditLogsByEntity("PROJECT", id);
    }

    private ProjectResponseDto mapToDto(Project p) {
        return ProjectResponseDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientName(p.getClient() != null ? p.getClient().getName() : null)
                .companyName(p.getClient() != null ? p.getClient().getCompanyName() : null)
                .createdByUserId(p.getCreatedByUser() != null ? p.getCreatedByUser().getId() : null)
                .createdByUserName(p.getCreatedByUser() != null ? p.getCreatedByUser().getName() : null)
                .assignedToUserId(p.getAssignedTo() != null ? p.getAssignedTo().getId() : null)
                .assignedToUserName(p.getAssignedTo() != null ? p.getAssignedTo().getName() : null)
                .statusId(p.getStatus() != null ? p.getStatus().getId() : null)
                .statusName(p.getStatus() != null ? p.getStatus().getName() : null)
                .expectedValue(p.getExpectedValue())
                .expectedRoyalty(p.getExpectedRoyalty())
                .expectedCommission(p.getExpectedCommission())
                .startDate(p.getStartDate())
                .expectedCloseDate(p.getExpectedCloseDate())
                .actualCloseDate(p.getActualCloseDate())
                .build();
    }
}
