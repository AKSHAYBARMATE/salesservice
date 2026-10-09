package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalRequestDto;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalResponseDto;
import com.projectmanagement.seller.service.ProjectProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/proposals")
@RequiredArgsConstructor
public class ProjectProposalController {

    private final ProjectProposalService proposalService;

    @PostMapping("/createProposal/{projectId}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> createProposal(
            @PathVariable Long projectId,
            @Valid @RequestBody ProposalRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proposalService.createProposal(projectId, requestDto));
    }

    @GetMapping("/getProposalsByProjectId/{projectId}")
    public ResponseEntity<StandardResponse<List<ProposalResponseDto>>> getProposalsByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(proposalService.getProposalsByProject(projectId));
    }

    @GetMapping("/getProposalById/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> getProposalById(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.getProposalById(id));
    }

    @PutMapping("/updateProposal/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> updateProposal(
            @PathVariable Long id,
            @Valid @RequestBody ProposalRequestDto requestDto
    ) {
        return ResponseEntity.ok(proposalService.updateProposal(id, requestDto));
    }

    @PostMapping("/submitProposal/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> submitProposal(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.submitProposal(id));
    }

    @PostMapping("/approveProposal/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> approveProposal(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.approveProposal(id));
    }

    @PostMapping("/rejectProposal/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> rejectProposal(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.rejectProposal(id));
    }

    @PostMapping("/acceptProposal/{id}")
    public ResponseEntity<StandardResponse<ProposalResponseDto>> acceptProposal(@PathVariable Long id) {
        return ResponseEntity.ok(proposalService.acceptProposal(id));
    }
}
