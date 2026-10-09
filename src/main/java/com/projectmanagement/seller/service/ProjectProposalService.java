package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalRequestDto;
import com.projectmanagement.seller.dto.ProposalDtos.ProposalResponseDto;

import java.util.List;

public interface ProjectProposalService {
    StandardResponse<ProposalResponseDto> createProposal(Long projectId, ProposalRequestDto requestDto);
    StandardResponse<List<ProposalResponseDto>> getProposalsByProject(Long projectId);
    StandardResponse<ProposalResponseDto> getProposalById(Long id);
    StandardResponse<ProposalResponseDto> updateProposal(Long id, ProposalRequestDto requestDto);
    StandardResponse<ProposalResponseDto> submitProposal(Long id);
    StandardResponse<ProposalResponseDto> approveProposal(Long id);
    StandardResponse<ProposalResponseDto> rejectProposal(Long id);
    StandardResponse<ProposalResponseDto> acceptProposal(Long id);
}
