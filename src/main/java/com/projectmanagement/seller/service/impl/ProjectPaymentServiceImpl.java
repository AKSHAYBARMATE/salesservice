package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentRequestDto;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentResponseDto;
import com.projectmanagement.seller.entity.Project;
import com.projectmanagement.seller.entity.ProjectPayment;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ProjectPaymentRepository;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.ProjectPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectPaymentServiceImpl implements ProjectPaymentService {

    private final ProjectPaymentRepository paymentRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<PaymentResponseDto> createPayment(Long projectId, PaymentRequestDto requestDto) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null) {
            throw new CustomException("Payment payload is required", "INVALID_INPUT");
        }
        if (requestDto.getAmount() == null || requestDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("Payment amount must be greater than zero", "INVALID_AMOUNT");
        }
        if (requestDto.getPaymentType() == null || requestDto.getPaymentType().trim().isEmpty()) {
            throw new CustomException("Payment type is required (One Time / Recurring)", "INVALID_INPUT");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        ProjectPayment payment = ProjectPayment.builder()
                .project(project)
                .paymentType(requestDto.getPaymentType().trim())
                .amount(requestDto.getAmount())
                .paymentDate(requestDto.getPaymentDate())
                .nextPaymentDate(requestDto.getNextPaymentDate())
                .billingPeriod(requestDto.getBillingPeriod())
                .status(requestDto.getStatus() != null ? requestDto.getStatus() : "Pending")
                .notes(requestDto.getNotes())
                .build();

        ProjectPayment saved = paymentRepository.save(payment);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_PAYMENT",
                "PAYMENT",
                saved.getId(),
                null,
                "Payment recorded: amount=" + saved.getAmount() + ", type=" + saved.getPaymentType() + ", status=" + saved.getStatus(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Payment recorded successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<PaymentResponseDto>> getPaymentsByProject(Long projectId) {
        if (projectId == null) {
            throw new CustomException("Project ID cannot be null", "INVALID_INPUT");
        }
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }

        List<PaymentResponseDto> payments = paymentRepository.findByProjectIdOrderByPaymentDateDesc(projectId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(payments, "Project payments fetched successfully");
    }

    private PaymentResponseDto mapToDto(ProjectPayment p) {
        return PaymentResponseDto.builder()
                .id(p.getId())
                .projectId(p.getProject().getId())
                .paymentType(p.getPaymentType())
                .amount(p.getAmount())
                .paymentDate(p.getPaymentDate())
                .nextPaymentDate(p.getNextPaymentDate())
                .billingPeriod(p.getBillingPeriod())
                .status(p.getStatus())
                .notes(p.getNotes())
                .build();
    }
}
