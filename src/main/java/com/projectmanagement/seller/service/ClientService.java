package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.ClientDtos.ClientRequestDto;
import com.projectmanagement.seller.dto.ClientDtos.ClientResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.ProjectResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    StandardResponse<ClientResponseDto> createClient(ClientRequestDto requestDto);
    StandardResponse<Page<ClientResponseDto>> getClients(String search, String status, Pageable pageable);
    StandardResponse<ClientResponseDto> getClientById(Long id);
    StandardResponse<ClientResponseDto> updateClient(Long id, ClientRequestDto requestDto);
    StandardResponse<List<ProjectResponseDto>> getClientProjects(Long clientId);
}
