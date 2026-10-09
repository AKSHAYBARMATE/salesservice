package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.RoleDtos.RoleRequestDto;
import com.projectmanagement.seller.dto.RoleDtos.RoleResponseDto;
import com.projectmanagement.seller.entity.Role;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.RoleRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final LoginUser loginUser;

    @Override
    @Transactional
    public StandardResponse<RoleResponseDto> createRole(RoleRequestDto requestDto) {
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Role name is required", "INVALID_INPUT");
        }

        String roleName = requestDto.getName().trim().toUpperCase();
        if (roleRepository.existsByNameIgnoreCase(roleName)) {
            throw new CustomException("Role already exists with name: " + roleName, "DUPLICATE_ROLE");
        }

        Role role = Role.builder()
                .name(roleName)
                .description(requestDto.getDescription())
                .build();

        Role saved = roleRepository.save(role);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_ROLE",
                "ROLE",
                saved.getId(),
                null,
                "Role created: " + saved.getName(),
                null
        );

        return StandardResponse.success(mapToDto(saved), "Role created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<RoleResponseDto>> getAllRoles() {
        List<RoleResponseDto> roles = roleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        return StandardResponse.success(roles, "Roles fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<RoleResponseDto> getRoleById(Long id) {
        if (id == null) {
            throw new CustomException("Role ID cannot be null", "INVALID_INPUT");
        }
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return StandardResponse.success(mapToDto(role), "Role details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<RoleResponseDto> updateRole(Long id, RoleRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("Role ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("Role name is required for update", "INVALID_INPUT");
        }

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        String newRoleName = requestDto.getName().trim().toUpperCase();
        if (!role.getName().equalsIgnoreCase(newRoleName) && roleRepository.existsByNameIgnoreCase(newRoleName)) {
            throw new CustomException("Another role with name '" + newRoleName + "' already exists", "DUPLICATE_ROLE");
        }

        String oldVal = "name=" + role.getName() + ", desc=" + role.getDescription();

        role.setName(newRoleName);
        role.setDescription(requestDto.getDescription());
        Role updated = roleRepository.save(role);

        String newVal = "name=" + updated.getName() + ", desc=" + updated.getDescription();

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_ROLE",
                "ROLE",
                updated.getId(),
                oldVal,
                newVal,
                null
        );

        return StandardResponse.success(mapToDto(updated), "Role updated successfully");
    }

    private RoleResponseDto mapToDto(Role role) {
        return RoleResponseDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}
