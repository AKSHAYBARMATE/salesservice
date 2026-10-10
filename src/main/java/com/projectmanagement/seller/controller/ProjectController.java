package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuditLogDtos.AuditLogResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.*;
import com.projectmanagement.seller.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/createProject")
    public ResponseEntity<StandardResponse<ProjectResponseDto>> createProject(@Valid @RequestBody ProjectCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(requestDto));
    }

    @GetMapping("/getAllProjects")
    public ResponseEntity<StandardResponse<Page<ProjectResponseDto>>> getAllProjects(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long assignedTo,
            @RequestParam(required = false) Long statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(projectService.getProjects(search, clientId, assignedTo, statusId, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/getProjectById/{id}")
    public ResponseEntity<StandardResponse<ProjectResponseDto>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PutMapping("/updateProject/{id}")
    public ResponseEntity<StandardResponse<ProjectResponseDto>> updateProject(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequestDto requestDto) {
        return ResponseEntity.ok(projectService.updateProject(id, requestDto));
    }

    @PatchMapping("/updateProjectStatus/{id}")
    public ResponseEntity<StandardResponse<ProjectResponseDto>> updateProjectStatus(@PathVariable Long id, @Valid @RequestBody ProjectStatusUpdateRequestDto requestDto) {
        return ResponseEntity.ok(projectService.updateProjectStatus(id, requestDto));
    }

    @PatchMapping("/updatePrice/{id}")
    public ResponseEntity<StandardResponse<ProjectResponseDto>> updatePrice(@PathVariable Long id, @Valid @RequestBody ProjectPriceUpdateRequestDto requestDto) {
        return ResponseEntity.ok(projectService.updatePrice(id, requestDto));
    }

    @GetMapping("/getProjectTimeline/{id}")
    public ResponseEntity<StandardResponse<List<AuditLogResponseDto>>> getProjectTimeline(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectTimeline(id));
    }
}
