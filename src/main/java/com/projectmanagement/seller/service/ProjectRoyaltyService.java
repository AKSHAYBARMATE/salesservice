package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectRoyaltyResponseDto;

import java.util.List;

public interface ProjectRoyaltyService {
    StandardResponse<List<ProjectRoyaltyResponseDto>> getRoyaltiesByProject(Long projectId);
    StandardResponse<List<ProjectRoyaltyResponseDto>> getUserRoyalties(Long userId);
}
