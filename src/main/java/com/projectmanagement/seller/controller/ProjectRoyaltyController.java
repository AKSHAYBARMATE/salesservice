package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.CommissionAndRoyaltyDtos.ProjectRoyaltyResponseDto;
import com.projectmanagement.seller.service.ProjectRoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/royalties")
@RequiredArgsConstructor
public class ProjectRoyaltyController {

    private final ProjectRoyaltyService royaltyService;

    @GetMapping("/getRoyaltyByProjectId/{projectId}")
    public ResponseEntity<StandardResponse<List<ProjectRoyaltyResponseDto>>> getRoyaltyByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(royaltyService.getRoyaltiesByProject(projectId));
    }

    @GetMapping("/getUserEarningsRoyalty")
    public ResponseEntity<StandardResponse<List<ProjectRoyaltyResponseDto>>> getUserEarningsRoyalty(
            @RequestParam(required = false) Long userId
    ) {
        return ResponseEntity.ok(royaltyService.getUserRoyalties(userId));
    }
}
