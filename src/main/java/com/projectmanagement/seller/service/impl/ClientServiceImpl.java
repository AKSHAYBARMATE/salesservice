package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.ClientDtos.ClientRequestDto;
import com.projectmanagement.seller.dto.ClientDtos.ClientResponseDto;
import com.projectmanagement.seller.dto.ProjectDtos.ProjectResponseDto;
import com.projectmanagement.seller.entity.Client;
import com.projectmanagement.seller.entity.Project;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.ClientRepository;
import com.projectmanagement.seller.repository.ProjectRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<ClientResponseDto> createClient(ClientRequestDto requestDto) {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Client name is required", "INVALID_INPUT");
        }

        Client client = Client.builder()
                .name(requestDto.getName().trim())
                .contactPerson(requestDto.getContactPerson())
                .email(requestDto.getEmail() != null ? requestDto.getEmail().trim().toLowerCase() : null)
                .phone(requestDto.getPhone())
                .companyName(requestDto.getCompanyName())
                .address(requestDto.getAddress())
                .status(requestDto.getStatus() != null ? requestDto.getStatus().toUpperCase() : "ACTIVE")
                .build();

        Client saved = clientRepository.save(client);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_CLIENT",
                "CLIENT",
                saved.getId(),
                null,
                "Created client: " + saved.getName() + " (" + saved.getCompanyName() + ")",
                null
        );

        return StandardResponse.success(mapToDto(saved), "Client created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<ClientResponseDto>> getClients(String search, String status, Pageable pageable) {
        Page<ClientResponseDto> page = clientRepository.findWithFilters(search, status, pageable)
                .map(this::mapToDto);

        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return StandardResponse.success(page, "Clients fetched successfully", metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<ClientResponseDto> getClientById(Long id) {
        if (id == null) {
            throw new CustomException("Client ID cannot be null", "INVALID_INPUT");
        }
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return StandardResponse.success(mapToDto(client), "Client details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<ClientResponseDto> updateClient(Long id, ClientRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Client ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Client name is required for update", "INVALID_INPUT");
        }

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        String oldVal = "name=" + client.getName() + ", company=" + client.getCompanyName();

        client.setName(requestDto.getName().trim());
        if (requestDto.getContactPerson() != null) client.setContactPerson(requestDto.getContactPerson());
        if (requestDto.getEmail() != null) client.setEmail(requestDto.getEmail().trim().toLowerCase());
        if (requestDto.getPhone() != null) client.setPhone(requestDto.getPhone());
        if (requestDto.getCompanyName() != null) client.setCompanyName(requestDto.getCompanyName());
        if (requestDto.getAddress() != null) client.setAddress(requestDto.getAddress());
        if (requestDto.getStatus() != null) client.setStatus(requestDto.getStatus().toUpperCase());

        Client updated = clientRepository.save(client);

        String newVal = "name=" + updated.getName() + ", company=" + updated.getCompanyName();

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_CLIENT",
                "CLIENT",
                updated.getId(),
                oldVal,
                newVal,
                null
        );

        return StandardResponse.success(mapToDto(updated), "Client updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<ProjectResponseDto>> getClientProjects(Long clientId) {
        if (clientId == null) {
            throw new CustomException("Client ID cannot be null", "INVALID_INPUT");
        }
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found with id: " + clientId);
        }

        List<ProjectResponseDto> projects = projectRepository.findByClientId(clientId).stream()
                .map(this::mapProjectToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(projects, "Client projects fetched successfully");
    }

    private ClientResponseDto mapToDto(Client client) {
        return ClientResponseDto.builder()
                .id(client.getId())
                .name(client.getName())
                .contactPerson(client.getContactPerson())
                .email(client.getEmail())
                .phone(client.getPhone())
                .companyName(client.getCompanyName())
                .address(client.getAddress())
                .status(client.getStatus())
                .build();
    }

    private ProjectResponseDto mapProjectToDto(Project p) {
        return ProjectResponseDto.builder()
                .id(p.getId())
                .title(p.getTitle())
                .description(p.getDescription())
                .clientId(p.getClient() != null ? p.getClient().getId() : null)
                .clientName(p.getClient() != null ? p.getClient().getName() : null)
                .companyName(p.getClient() != null ? p.getClient().getCompanyName() : null)
                .createdByUserId(p.getCreatedByUser() != null ? p.getCreatedByUser().getId() : null)
                .createdByUserName(p.getCreatedByUser() != null ? p.getCreatedByUser().getName() : null)
                .assignedToUserId(p.getAssignedTo() != null ? p.getAssignedTo().getId() : null)
                .assignedToUserName(p.getAssignedTo() != null ? p.getAssignedTo().getName() : null)
                .statusId(p.getStatus() != null ? p.getStatus().getId() : null)
                .statusName(p.getStatus() != null ? p.getStatus().getName() : null)
                .expectedValue(p.getExpectedValue())
                .expectedRoyalty(p.getExpectedRoyalty())
                .expectedCommission(p.getExpectedCommission())
                .startDate(p.getStartDate())
                .expectedCloseDate(p.getExpectedCloseDate())
                .actualCloseDate(p.getActualCloseDate())
                .build();
    }
}
