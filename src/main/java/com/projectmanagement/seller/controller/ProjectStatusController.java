package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusRequestDto;
import com.projectmanagement.seller.dto.ProjectStatusDtos.ProjectStatusResponseDto;
import com.projectmanagement.seller.service.ProjectStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/project-statuses")
@RequiredArgsConstructor
public class ProjectStatusController {

    private final ProjectStatusService projectStatusService;

    @PostMapping("/createProjectStatus")
    public ResponseEntity<StandardResponse<ProjectStatusResponseDto>> createProjectStatus(@Valid @RequestBody ProjectStatusRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectStatusService.createStatus(requestDto));
    }

    @GetMapping("/getAllProjectStatuses")
    public ResponseEntity<StandardResponse<List<ProjectStatusResponseDto>>> getAllProjectStatuses() {
        return ResponseEntity.ok(projectStatusService.getAllStatuses());
    }

    @GetMapping("/getProjectStatusById/{id}")
    public ResponseEntity<StandardResponse<ProjectStatusResponseDto>> getProjectStatusById(@PathVariable Long id) {
        return ResponseEntity.ok(projectStatusService.getStatusById(id));
    }

    @PutMapping("/updateProjectStatus/{id}")
    public ResponseEntity<StandardResponse<ProjectStatusResponseDto>> updateProjectStatus(@PathVariable Long id, @Valid @RequestBody ProjectStatusRequestDto requestDto) {
        return ResponseEntity.ok(projectStatusService.updateStatus(id, requestDto));
    }
}
