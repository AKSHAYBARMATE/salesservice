package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.CalculateCommissionRequestDto;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectCommissionResponseDto;

import java.util.List;

public interface ProjectCommissionService {
    StandardResponse<List<ProjectCommissionResponseDto>> getCommissionsByProject(Long projectId);
    StandardResponse<List<ProjectCommissionResponseDto>> getUserCommissions(Long userId);
    StandardResponse<ProjectCommissionResponseDto> calculateAndSaveCommission(Long projectId, CalculateCommissionRequestDto requestDto);
}
