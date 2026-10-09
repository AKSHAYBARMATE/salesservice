package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import com.projectmanagement.seller.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/getAuditLogs")
    public ResponseEntity<StandardResponse<Page<AuditLogResponseDto>>> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(auditLogService.getAuditLogs(userId, entityType, entityId, action, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/getAuditLogsByUser/{userId}")
    public ResponseEntity<StandardResponse<List<AuditLogResponseDto>>> getAuditLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByUser(userId));
    }

    @GetMapping("/getAuditLogsByEntity/{entityType}/{entityId}")
    public ResponseEntity<StandardResponse<List<AuditLogResponseDto>>> getAuditLogsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId
    ) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByEntity(entityType, entityId));
    }
}
