package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.CalculateCommissionRequestDto;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectCommissionResponseDto;
import com.projectmanagement.seller.service.ProjectCommissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/commissions")
@RequiredArgsConstructor
public class ProjectCommissionController {

    private final ProjectCommissionService commissionService;

    @GetMapping("/getCommissionByProjectId/{projectId}")
    public ResponseEntity<StandardResponse<List<ProjectCommissionResponseDto>>> getCommissionByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(commissionService.getCommissionsByProject(projectId));
    }

    @PostMapping("/calculateCommission/{projectId}")
    public ResponseEntity<StandardResponse<ProjectCommissionResponseDto>> calculateCommission(
            @PathVariable Long projectId,
            @RequestBody(required = false) CalculateCommissionRequestDto requestDto
    ) {
        if (requestDto == null) {
            requestDto = new CalculateCommissionRequestDto();
        }
        return ResponseEntity.ok(commissionService.calculateAndSaveCommission(projectId, requestDto));
    }

    @GetMapping("/getUserEarningsCommission")
    public ResponseEntity<StandardResponse<List<ProjectCommissionResponseDto>>> getUserEarningsCommission(
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(commissionService.getUserCommissions(userId));
    }
}
