package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelRequestDto;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelResponseDto;
import com.projectmanagement.seller.entity.SalesLevel;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.SalesLevelRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.SalesLevelService;
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
public class SalesLevelServiceImpl implements SalesLevelService {

    private final SalesLevelRepository salesLevelRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<SalesLevelResponseDto> createSalesLevel(SalesLevelRequestDto requestDto) {
        if (requestDto == null || requestDto.getLevelName() == null || requestDto.getLevelName().trim().isEmpty()) {
            throw new CustomException("Sales level name is required", "INVALID_INPUT");
        }

        String levelName = requestDto.getLevelName().trim();
        if (salesLevelRepository.existsByLevelNameIgnoreCase(levelName)) {
            throw new CustomException("Sales level already exists with name: " + levelName, "DUPLICATE_LEVEL");
        }

        if (requestDto.getCommissionRate() != null && requestDto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Commission rate cannot be negative", "INVALID_RATE");
        }

        if (requestDto.getRoyaltyRate() != null && requestDto.getRoyaltyRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Royalty rate cannot be negative", "INVALID_RATE");
        }

        SalesLevel salesLevel = SalesLevel.builder()
                .levelName(levelName)
                .minProjects(requestDto.getMinProjects() != null ? requestDto.getMinProjects() : 0)
                .commissionRate(requestDto.getCommissionRate())
                .royaltyRate(requestDto.getRoyaltyRate())
                .description(requestDto.getDescription())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        SalesLevel saved = salesLevelRepository.save(salesLevel);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_SALES_LEVEL",
                "SALES_LEVEL",
                saved.getId(),
                null,
                "Sales level created: " + saved.getLevelName(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Sales level created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<SalesLevelResponseDto>> getAllSalesLevels() {
        List<SalesLevelResponseDto> levels = salesLevelRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return StandardResponse.success(levels, "Sales levels fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<SalesLevelResponseDto> getSalesLevelById(Long id) {
        if (id == null) {
            throw new CustomException("Sales level ID cannot be null", "INVALID_INPUT");
        }
        SalesLevel salesLevel = salesLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales level not found with id: " + id));
        return StandardResponse.success(mapToDto(salesLevel), "Sales level details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<SalesLevelResponseDto> updateSalesLevel(Long id, SalesLevelRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Sales level ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getLevelName() == null || requestDto.getLevelName().trim().isEmpty()) {
            throw new CustomException("Sales level name is required for update", "INVALID_INPUT");
        }

        SalesLevel salesLevel = salesLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales level not found with id: " + id));

        String newName = requestDto.getLevelName().trim();
        if (!salesLevel.getLevelName().equalsIgnoreCase(newName) && salesLevelRepository.existsByLevelNameIgnoreCase(newName)) {
            throw new CustomException("Another sales level with name '" + newName + "' already exists", "DUPLICATE_LEVEL");
        }

        if (requestDto.getCommissionRate() != null && requestDto.getCommissionRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Commission rate cannot be negative", "INVALID_RATE");
        }

        if (requestDto.getRoyaltyRate() != null && requestDto.getRoyaltyRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new CustomException("Royalty rate cannot be negative", "INVALID_RATE");
        }

        String oldVal = "name=" + salesLevel.getLevelName() + ", commission=" + salesLevel.getCommissionRate() + ", royalty=" + salesLevel.getRoyaltyRate();

        salesLevel.setLevelName(newName);
        if (requestDto.getMinProjects() != null) salesLevel.setMinProjects(requestDto.getMinProjects());
        if (requestDto.getCommissionRate() != null) salesLevel.setCommissionRate(requestDto.getCommissionRate());
        if (requestDto.getRoyaltyRate() != null) salesLevel.setRoyaltyRate(requestDto.getRoyaltyRate());
        if (requestDto.getDescription() != null) salesLevel.setDescription(requestDto.getDescription());
        if (requestDto.getIsActive() != null) salesLevel.setIsActive(requestDto.getIsActive());

        SalesLevel updated = salesLevelRepository.save(salesLevel);

        String newVal = "name=" + updated.getLevelName() + ", commission=" + updated.getCommissionRate() + ", royalty=" + updated.getRoyaltyRate();

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_SALES_LEVEL",
                "SALES_LEVEL",
                updated.getId(),
                oldVal,
                newVal,
                null
        );

        return StandardResponse.success(mapToDto(updated), "Sales level updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteSalesLevel(Long id) {
        if (id == null) {
            throw new CustomException("Sales level ID cannot be null", "INVALID_INPUT");
        }
        SalesLevel salesLevel = salesLevelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sales level not found with id: " + id));
        salesLevel.setIsActive(false);
        salesLevel.setIsDeleted(true);
        salesLevelRepository.save(salesLevel);

        auditLogService.log(
                loginUser.getUserId(),
                "DELETE_SALES_LEVEL",
                "SALES_LEVEL",
                id,
                "active=true",
                "active=false, deleted=true",
                null
        );

        return StandardResponse.success("Sales level deactivated successfully");
    }

    private SalesLevelResponseDto mapToDto(SalesLevel salesLevel) {
        return SalesLevelResponseDto.builder()
                .id(salesLevel.getId())
                .levelName(salesLevel.getLevelName())
                .minProjects(salesLevel.getMinProjects())
                .commissionRate(salesLevel.getCommissionRate())
                .royaltyRate(salesLevel.getRoyaltyRate())
                .description(salesLevel.getDescription())
                .isActive(salesLevel.getIsActive())
                .build();
    }
}
