package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalRequestDto;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalResponseDto;
import com.projectmanagement.seller.entity.Project;
import com.projectmanagement.seller.entity.ProjectProposal;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectProposalRepository;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.NotificationService;
import com.projectmanagement.seller.service.ProjectProposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectProposalServiceImpl implements ProjectProposalService {

    private final ProjectProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> createProposal(Long projectId, ProposalRequestDto requestDto) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getProposalName() == null || requestDto.getProposalName().trim().isEmpty()) {
            throw new CustomException("Proposal name is required", "INVALID_INPUT");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (requestDto.getOneTimePrice() != null && requestDto.getOneTimePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("One-time price cannot be negative", "INVALID_PRICE");
        }
        if (requestDto.getRecurringAmount() != null && requestDto.getRecurringAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Recurring amount cannot be negative", "INVALID_PRICE");
        }

        List<ProjectProposal> existingProposals = proposalRepository.findByProjectIdOrderByVersionDesc(projectId);
        int nextVersion = existingProposals.isEmpty() ? 1 : existingProposals.get(0).getVersion() + 1;

        ProjectProposal proposal = ProjectProposal.builder()
                .project(project)
                .proposalName(requestDto.getProposalName().trim())
                .content(requestDto.getContent())
                .oneTimePrice(requestDto.getOneTimePrice())
                .recurringAmount(requestDto.getRecurringAmount())
                .billingFrequency(requestDto.getBillingFrequency())
                .validityDate(requestDto.getValidityDate())
                .version(requestDto.getVersion() != null ? requestDto.getVersion() : nextVersion)
                .status("Draft")
                .sentDate(requestDto.getSentDate())
                .build();

        ProjectProposal saved = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_PROPOSAL",
                "PROPOSAL",
                saved.getId(),
                null,
                "Created proposal: " + saved.getProposalName() + " v" + saved.getVersion(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Proposal created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProposalResponseDto>> getProposalsByProject(Long projectId) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<ProposalResponseDto> proposals = proposalRepository.findByProjectIdOrderByVersionDesc(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(proposals, "Project proposals fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ProposalResponseDto> getProposalById(Long id) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));
        return StandardResponse.success(mapToDto(proposal), "Proposal details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> updateProposal(Long id, ProposalRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null) {
            throw new CustomException("Update payload is required", "INVALID_INPUT");
        }

        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        if (requestDto.getProposalName() != null && !requestDto.getProposalName().trim().isEmpty()) {
            proposal.setProposalName(requestDto.getProposalName().trim());
        }
        if (requestDto.getContent() != null) proposal.setContent(requestDto.getContent());
        if (requestDto.getOneTimePrice() != null) {
            if (requestDto.getOneTimePrice().compareTo(BigDecimal.ZERO) < 0) {
                throw new CustomException("One-time price cannot be negative", "INVALID_PRICE");
            }
            proposal.setOneTimePrice(requestDto.getOneTimePrice());
        }
        if (requestDto.getRecurringAmount() != null) {
            if (requestDto.getRecurringAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new CustomException("Recurring amount cannot be negative", "INVALID_PRICE");
            }
            proposal.setRecurringAmount(requestDto.getRecurringAmount());
        }
        if (requestDto.getBillingFrequency() != null) proposal.setBillingFrequency(requestDto.getBillingFrequency());
        if (requestDto.getValidityDate() != null) proposal.setValidityDate(requestDto.getValidityDate());
        if (requestDto.getStatus() != null) proposal.setStatus(requestDto.getStatus());
        if (requestDto.getSentDate() != null) proposal.setSentDate(requestDto.getSentDate());

        ProjectProposal updated = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_PROPOSAL",
                "PROPOSAL",
                updated.getId(),
                null,
                "Updated proposal: " + updated.getProposalName(),
                null
        );

        return StandardResponse.success(mapToDto(updated), "Proposal updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> submitProposal(Long id) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        proposal.setStatus("Submitted");
        ProjectProposal updated = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "SUBMIT_PROPOSAL",
                "PROPOSAL",
                updated.getId(),
                "status=Draft",
                "status=Submitted",
                null
        );

        return StandardResponse.success(mapToDto(updated), "Proposal submitted for review successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> approveProposal(Long id) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        User approver = null;
        if (loginUser.getUserId() != null) {
            approver = userRepository.findById(loginUser.getUserId()).orElse(null);
        }

        proposal.setStatus("Approved");
        proposal.setApprovedBy(approver);
        proposal.setApprovedAt(LocalDateTime.now());
        ProjectProposal updated = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "APPROVE_PROPOSAL",
                "PROPOSAL",
                updated.getId(),
                null,
                "Proposal approved by: " + (approver != null ? approver.getName() : "Admin"),
                null
        );

        return StandardResponse.success(mapToDto(updated), "Proposal approved successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> rejectProposal(Long id) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        proposal.setStatus("Rejected");
        ProjectProposal updated = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "REJECT_PROPOSAL",
                "PROPOSAL",
                updated.getId(),
                null,
                "Proposal rejected",
                null
        );

        return StandardResponse.success(mapToDto(updated), "Proposal rejected successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ProposalResponseDto> acceptProposal(Long id) {
        if (id == null) {
            throw new CustomException("Proposal ID cannot be null", "INVALID_INPUT");
        }
        ProjectProposal proposal = proposalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));

        proposal.setStatus("Accepted");
        ProjectProposal updated = proposalRepository.save(proposal);

        auditLogService.log(
                loginUser.getUserId(),
                "ACCEPT_PROPOSAL",
                "PROPOSAL",
                updated.getId(),
                null,
                "Proposal marked as Accepted by client",
                null
        );

        return StandardResponse.success(mapToDto(updated), "Proposal marked as accepted successfully");
    }

    private ProposalResponseDto mapToDto(ProjectProposal p) {
        return ProposalResponseDto.builder()
                .id(p.getId())
                .projectId(p.getProject().getId())
                .proposalName(p.getProposalName())
                .content(p.getContent())
                .oneTimePrice(p.getOneTimePrice())
                .recurringAmount(p.getRecurringAmount())
                .billingFrequency(p.getBillingFrequency())
                .validityDate(p.getValidityDate())
                .version(p.getVersion())
                .status(p.getStatus())
                .sentDate(p.getSentDate())
                .approvedById(p.getApprovedBy() != null ? p.getApprovedBy().getId() : null)
                .approvedByName(p.getApprovedBy() != null ? p.getApprovedBy().getName() : null)
                .approvedAt(p.getApprovedAt())
                .build();
    }
}
