package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {
    void log(Long userId, String action, String entityType, Long entityId, String oldValue, String newValue, String ipAddress);
    StandardResponse<Page<AuditLogResponseDto>> getAuditLogs(Long userId, String entityType, Long entityId, String action, Pageable pageable);
    StandardResponse<List<AuditLogResponseDto>> getAuditLogsByEntity(String entityType, Long entityId);
    StandardResponse<List<AuditLogResponseDto>> getAuditLogsByUser(Long userId);
}
