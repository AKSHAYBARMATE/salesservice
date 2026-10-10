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
    private final SalesLevelRepository salesLevelRepository;
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

        if (loginUser.getUserId() == null) {
            throw new CustomException("Authentication required to create a project", "UNAUTHORIZED");
        }

        User currentUser = userRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + loginUser.getUserId()));

        String roleName = currentUser.getRole() != null ? currentUser.getRole().getName() : null;
        if (roleName == null || (!"SALES".equalsIgnoreCase(roleName) && !"SELLER".equalsIgnoreCase(roleName))) {
            throw new CustomException("Only users with SALES role can create projects", "ACCESS_DENIED");
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

        Project project = Project.builder()
                .title(requestDto.getTitle().trim())
                .description(requestDto.getDescription())
                .client(client)
                .createdByUser(createdByUser)
                .assignedTo(assignedTo)
                .status(status)
                .expectedValue(BigDecimal.ZERO)
                .expectedCommission(BigDecimal.ZERO)
                .expectedRoyalty(BigDecimal.ZERO)
                .startDate(requestDto.getStartDate())
                .expectedCloseDate(requestDto.getExpectedCloseDate())
                .build();

        Project saved = projectRepository.save(project);

        if (assignedTo != null) {
            updateSalesUserLevel(assignedTo);
        }

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
        Long effectiveAssignedTo = assignedTo;

        if (loginUser.getUserId() != null) {
            String role = loginUser.getRole();
            if (role == null || role.isBlank()) {
                User currentUser = userRepository.findById(loginUser.getUserId()).orElse(null);
                if (currentUser != null && currentUser.getRole() != null) {
                    role = currentUser.getRole().getName();
                }
            }

            // If user role is SALES / SELLER, only fetch projects assigned to them
            if ("SALES".equalsIgnoreCase(role) || "SELLER".equalsIgnoreCase(role)) {
                effectiveAssignedTo = loginUser.getUserId();
            }
            // If user role is ADMIN, fetch all projects (or filter by assignedTo query param if provided)
        }

        Page<ProjectResponseDto> page = projectRepository.findWithFilters(search, clientId, effectiveAssignedTo, statusId, pageable)
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

        if (requestDto.getAssignedTo() != null) {
            User assignedTo = userRepository.findById(requestDto.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales person not found with id: " + requestDto.getAssignedTo()));
            project.setAssignedTo(assignedTo);
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

        String oldStatusName = project.getStatus() != null ? project.getStatus().getName() : "None";
        String newStatusName = newStatus.getName();

        project.setStatus(newStatus);

        boolean isWon = Boolean.TRUE.equals(newStatus.getIsWonStatus()) || "Won".equalsIgnoreCase(newStatusName);
        boolean isLost = "Lost".equalsIgnoreCase(newStatusName);
        boolean isApproved = "Approved".equalsIgnoreCase(newStatusName);
        boolean isSubmitted = "Submitted".equalsIgnoreCase(newStatusName);

        String auditDetails = null;

        if (isWon) {
            project.setActualCloseDate(LocalDate.now());

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
                        .project(project)
                        .user(assignedUser)
                        .salesLevel(level)
                        .commissionRate(commRate)
                        .commissionAmount(commAmount)
                        .status("Pending")
                        .build();
                commissionRepository.save(commission);

                ProjectRoyalty royalty = ProjectRoyalty.builder()
                        .project(project)
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
                assignedUser.setTotalCommission(assignedUser.getTotalCommission().add(commAmount));
                assignedUser.setTotalRoyalty(assignedUser.getTotalRoyalty().add(royAmount));
                userRepository.save(assignedUser);

                notificationService.sendNotification(
                        assignedUser.getId(),
                        "Project Won!",
                        "Congratulations! Project '" + project.getTitle() + "' marked as WON. Expected commission: $" + commAmount
                );
            }
            auditDetails = "Project marked as WON. Commission and Royalty recorded.";
        } else if (isLost) {
            project.setActualCloseDate(LocalDate.now());
            if (project.getAssignedTo() != null) {
                notificationService.sendNotification(
                        project.getAssignedTo().getId(),
                        "Project Closed - Lost",
                        "Project '" + project.getTitle() + "' has been marked as LOST."
                );
            }
            auditDetails = "Project marked as LOST";
        } else if (isApproved) {
            if (project.getAssignedTo() != null) {
                notificationService.sendNotification(
                        project.getAssignedTo().getId(),
                        "Project Approved",
                        "Project '" + project.getTitle() + "' has been approved by admin."
                );
            }
            auditDetails = "Project approved by admin";
        } else if (isSubmitted) {
            auditDetails = "Project submitted for internal review";
        }

        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "STATUS_CHANGE",
                "PROJECT",
                updated.getId(),
                "status=" + oldStatusName,
                "status=" + newStatusName,
                auditDetails
        );

        return StandardResponse.success(mapToDto(updated), "Project status updated successfully to " + newStatusName);
    }

    @Override
    @Transactional
    public StandardResponse<ProjectResponseDto> updatePrice(Long id, ProjectPriceUpdateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getExpectedValue() == null) {
            throw new CustomException("Expected price / value is required", "INVALID_INPUT");
        }
        if (requestDto.getExpectedValue().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Expected value cannot be negative", "INVALID_VALUE");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        BigDecimal oldExpectedValue = project.getExpectedValue();
        BigDecimal expectedValue = requestDto.getExpectedValue();
        BigDecimal expectedCommission = requestDto.getExpectedCommission();
        BigDecimal expectedRoyalty = requestDto.getExpectedRoyalty();

        // Auto calculate expected commission and royalty based on assigned user's sales level if not provided
        User assignedTo = project.getAssignedTo();
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

        project.setExpectedValue(expectedValue);
        project.setExpectedCommission(expectedCommission != null ? expectedCommission : BigDecimal.ZERO);
        project.setExpectedRoyalty(expectedRoyalty != null ? expectedRoyalty : BigDecimal.ZERO);

        Project updated = projectRepository.save(project);

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_PRICE",
                "PROJECT",
                updated.getId(),
                "expectedValue=" + oldExpectedValue,
                "expectedValue=" + expectedValue + ", expectedCommission=" + project.getExpectedCommission() + ", expectedRoyalty=" + project.getExpectedRoyalty(),
                null
        );

        return StandardResponse.success(mapToDto(updated), "Project price updated successfully");
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

    private void updateSalesUserLevel(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        long projectCount = projectRepository.countByAssignedToIdAndIsDeletedFalse(user.getId());
        user.setTotalProjects((int) projectCount);

        List<SalesLevel> levels = salesLevelRepository.findByIsActiveTrueOrderByMinProjectsDesc();
        if (levels != null && !levels.isEmpty()) {
            SalesLevel matchedLevel = levels.stream()
                    .filter(lvl -> lvl.getMinProjects() != null && projectCount >= lvl.getMinProjects())
                    .findFirst()
                    .orElse(levels.get(levels.size() - 1)); // Fallback to lowest level (e.g. Bronze)

            SalesLevel currentLevel = user.getSalesLevel();
            if (currentLevel == null || !currentLevel.getId().equals(matchedLevel.getId())) {
                log.info("Auto-updating user {} (id: {}) sales level from {} to {} (project count: {})",
                        user.getName(), user.getId(),
                        (currentLevel != null ? currentLevel.getLevelName() : "None"),
                        matchedLevel.getLevelName(),
                        projectCount);

                user.setSalesLevel(matchedLevel);

                auditLogService.log(
                        user.getId(),
                        "SALES_LEVEL_UPDATE",
                        "USER",
                        user.getId(),
                        "level=" + (currentLevel != null ? currentLevel.getLevelName() : "None"),
                        "level=" + matchedLevel.getLevelName() + ", totalProjects=" + projectCount,
                        "Auto-updated sales level based on database min_projects threshold (" + projectCount + " projects)"
                );
            }
        }

        userRepository.save(user);
    }
}
