package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.RoleDtos.RoleRequestDto;
import com.projectmanagement.seller.dto.RoleDtos.RoleResponseDto;
import com.projectmanagement.seller.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/createRole")
    public ResponseEntity<StandardResponse<RoleResponseDto>> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(requestDto));
    }

    @GetMapping("/getAllRoles")
    public ResponseEntity<StandardResponse<List<RoleResponseDto>>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/getRoleById/{id}")
    public ResponseEntity<StandardResponse<RoleResponseDto>> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PutMapping("/updateRole/{id}")
    public ResponseEntity<StandardResponse<RoleResponseDto>> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequestDto requestDto) {
        return ResponseEntity.ok(roleService.updateRole(id, requestDto));
    }
}
