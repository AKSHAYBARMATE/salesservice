package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import com.projectmanagement.seller.entity.AuditLog;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.repository.AuditLogRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void log(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue, String ipAddress) {
        try {
            User user = null;
            if (userId != null) {
                user = userRepository.findById(userId).orElse(null);
            }

            AuditLog auditLog = AuditLog.builder()
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
            log.info("Audit log recorded: action={}, entityType={}, entityId={}, userId={}", action, entityType, entityId, userId);
        } catch (Exception e) {
            log.error("Failed to record audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<AuditLogResponseDto>> getAuditLogs(Long userId, String entityType, Long entityId, String action, Pageable pageable) {
        Page<AuditLogResponseDto> page = auditLogRepository.findWithFilters(userId, entityType, entityId, action, pageable)
                .map(this::mapToDto);

        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return StandardResponse.success(page, "Audit logs fetched successfully", metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<AuditLogResponseDto>> getAuditLogsByEntity(String entityType, Long entityId) {
        if (entityType == null || entityType.trim().isEmpty() || entityId == null) {
            throw new CustomException("Entity type and Entity ID are required", "INVALID_INPUT");
        }

        List<AuditLogResponseDto> list = auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedOnDesc(entityType.toUpperCase(), entityId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Entity audit logs fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<AuditLogResponseDto>> getAuditLogsByUser(Long userId) {
        if (userId == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }

        List<AuditLogResponseDto> list = auditLogRepository.findByUserIdOrderByCreatedOnDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "User audit logs fetched successfully");
    }

    private AuditLogResponseDto mapToDto(AuditLog auditLog) {
        return AuditLogResponseDto.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userName(auditLog.getUser() != null ? auditLog.getUser().getName() : "System")
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .oldValue(auditLog.getOldValue())
                .newValue(auditLog.getNewValue())
                .ipAddress(auditLog.getIpAddress())
                .createdOn(auditLog.getCreatedOn())
                .build();
    }
}
