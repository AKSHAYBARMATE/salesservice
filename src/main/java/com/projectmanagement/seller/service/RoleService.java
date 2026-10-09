package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.RoleDtos.RoleRequestDto;
import com.projectmanagement.seller.dto.RoleDtos.RoleResponseDto;

import java.util.List;

public interface RoleService {
    StandardResponse<RoleResponseDto> createRole(RoleRequestDto requestDto);
    StandardResponse<List<RoleResponseDto>> getAllRoles();
    StandardResponse<RoleResponseDto> getRoleById(Long id);
    StandardResponse<RoleResponseDto> updateRole(Long id, RoleRequestDto requestDto);
}
