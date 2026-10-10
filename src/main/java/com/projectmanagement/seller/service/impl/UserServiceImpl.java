package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.UserDtos.UserCreateRequestDto;
import com.projectmanagement.seller.dto.UserDtos.UserResponseDto;
import com.projectmanagement.seller.dto.UserDtos.UserStatusUpdateRequestDto;
import com.projectmanagement.seller.dto.UserDtos.UserUpdateRequestDto;
import com.projectmanagement.seller.entity.Role;
import com.projectmanagement.seller.entity.SalesLevel;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.RoleRepository;
import com.projectmanagement.seller.repository.SalesLevelRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.AuditLogService;
import com.projectmanagement.seller.service.NotificationService;
import com.projectmanagement.seller.service.UserService;
import com.projectmanagement.seller.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SalesLevelRepository salesLevelRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final LoginUser loginUser;
    private final PasswordEncoder passwordEncoder;
    private final EmailUtil emailUtil;

    private static final Set<String> VALID_STATUSES = Set.of("ACTIVE", "INACTIVE", "SUSPENDED");

    @Override
    @Transactional
    public StandardResponse<UserResponseDto> createUser(UserCreateRequestDto requestDto) {
        if (requestDto == null) {
            throw new CustomException("User request payload is required", "INVALID_INPUT");
        }
        if (requestDto.getName() == null || requestDto.getName().trim().isEmpty()) {
            throw new CustomException("User name is required", "INVALID_INPUT");
        }
        if (requestDto.getEmail() == null || requestDto.getEmail().trim().isEmpty()) {
            throw new CustomException("Email is required", "INVALID_INPUT");
        }

        String email = requestDto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new CustomException("User already exists with email: " + email, "DUPLICATE_EMAIL");
        }

        Role role = null;
        if (requestDto.getRoleId() != null) {
            role = roleRepository.findById(requestDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + requestDto.getRoleId()));
        }

        // Automatically assign Bronze sales level for newly created user (initial projects = 0)
        SalesLevel salesLevel = salesLevelRepository.findByLevelNameIgnoreCase("Bronze")
                .orElseGet(() -> salesLevelRepository.findByIsActiveTrueOrderByMinProjectsAsc().stream()
                        .findFirst()
                        .orElse(null));

        String status = requestDto.getStatus() != null ? requestDto.getStatus().toUpperCase() : "ACTIVE";
        if (!VALID_STATUSES.contains(status)) {
            throw new CustomException("Invalid status: " + status + ". Allowed values: " + VALID_STATUSES, "INVALID_STATUS");
        }

        String passwordHash = null;
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            passwordHash = passwordEncoder.encode(requestDto.getPassword());
        }

        User user = User.builder()
                .name(requestDto.getName().trim())
                .email(email)
                .phone(requestDto.getPhone())
                .passwordHash(passwordHash)
                .role(role)
                .salesLevel(salesLevel)
                .totalProjects(0)
                .totalCommission(BigDecimal.ZERO)
                .totalRoyalty(BigDecimal.ZERO)
                .status(status)
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(
                loginUser.getUserId(),
                "CREATE_USER",
                "USER",
                saved.getId(),
                null,
                "Created user: " + saved.getEmail() + ", role: " + (role != null ? role.getName() : "None") + ", level: " + (salesLevel != null ? salesLevel.getLevelName() : "None"),
                null
        );

        notificationService.sendNotification(
                saved.getId(),
                "Welcome to Seller Management System",
                "Your account has been successfully created."
        );

        // Send welcome email with login credentials
        emailUtil.sendUserCredentialsMail(
                saved.getEmail(),
                saved.getName(),
                saved.getEmail(),
                requestDto.getPassword()
        );

        return StandardResponse.success(mapToDto(saved), "User created successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<UserResponseDto>> getUsers(String search, Long roleId, Long levelId, String status, Pageable pageable) {
        Page<UserResponseDto> page = userRepository.findWithFilters(search, roleId, levelId, status, pageable)
                .map(this::mapToDto);

        StandardResponse.ResponseMetadata metadata = StandardResponse.ResponseMetadata.builder()
                .currentPage(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalRecords(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return StandardResponse.success(page, "Users fetched successfully", metadata);
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<UserResponseDto> getUserById(Long id) {
        if (id == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return StandardResponse.success(mapToDto(user), "User details fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<UserResponseDto> updateUser(Long id, UserUpdateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null) {
            throw new CustomException("Update request payload is required", "INVALID_INPUT");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String oldVal = "name=" + user.getName() + ", status=" + user.getStatus() +
                ", role=" + (user.getRole() != null ? user.getRole().getName() : "None") +
                ", level=" + (user.getSalesLevel() != null ? user.getSalesLevel().getLevelName() : "None");

        if (requestDto.getName() != null && !requestDto.getName().trim().isEmpty()) {
            user.setName(requestDto.getName().trim());
        }
        if (requestDto.getPhone() != null) user.setPhone(requestDto.getPhone());

        if (requestDto.getStatus() != null) {
            String status = requestDto.getStatus().toUpperCase();
            if (!VALID_STATUSES.contains(status)) {
                throw new CustomException("Invalid status: " + status + ". Allowed values: " + VALID_STATUSES, "INVALID_STATUS");
            }
            user.setStatus(status);
        }

        if (requestDto.getRoleId() != null) {
            Role role = roleRepository.findById(requestDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + requestDto.getRoleId()));
            user.setRole(role);
        }

        if (requestDto.getLevelId() != null) {
            SalesLevel level = salesLevelRepository.findById(requestDto.getLevelId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sales level not found with id: " + requestDto.getLevelId()));
            user.setSalesLevel(level);
        }

        User updated = userRepository.save(user);

        String newVal = "name=" + updated.getName() + ", status=" + updated.getStatus() +
                ", role=" + (updated.getRole() != null ? updated.getRole().getName() : "None") +
                ", level=" + (updated.getSalesLevel() != null ? updated.getSalesLevel().getLevelName() : "None");

        auditLogService.log(
                loginUser.getUserId(),
                "UPDATE_USER",
                "USER",
                updated.getId(),
                oldVal,
                newVal,
                null
        );

        return StandardResponse.success(mapToDto(updated), "User updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<UserResponseDto> updateUserStatus(Long id, UserStatusUpdateRequestDto requestDto) {
        if (id == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }
        if (requestDto == null || requestDto.getStatus() == null || requestDto.getStatus().trim().isEmpty()) {
            throw new CustomException("Status is required", "INVALID_INPUT");
        }

        String status = requestDto.getStatus().trim().toUpperCase();
        if (!VALID_STATUSES.contains(status)) {
            throw new CustomException("Invalid status: " + status + ". Allowed values: " + VALID_STATUSES, "INVALID_STATUS");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String oldStatus = user.getStatus();
        user.setStatus(status);
        User updated = userRepository.save(user);

        auditLogService.log(
                loginUser.getUserId(),
                "STATUS_CHANGE",
                "USER",
                updated.getId(),
                "status=" + oldStatus,
                "status=" + updated.getStatus(),
                null
        );

        return StandardResponse.success(mapToDto(updated), "User status updated successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> deleteUser(Long id) {
        if (id == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setIsDeleted(true);
        user.setStatus("INACTIVE");
        userRepository.save(user);

        auditLogService.log(
                loginUser.getUserId(),
                "DELETE_USER",
                "USER",
                id,
                "active=true",
                "active=false, isDeleted=true",
                null
        );

        return StandardResponse.success("User deleted successfully");
    }

    private UserResponseDto mapToDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .levelId(user.getSalesLevel() != null ? user.getSalesLevel().getId() : null)
                .levelName(user.getSalesLevel() != null ? user.getSalesLevel().getLevelName() : null)
                .totalProjects(user.getTotalProjects())
                .totalCommission(user.getTotalCommission())
                .totalRoyalty(user.getTotalRoyalty())
                .status(user.getStatus())
                .build();
    }
}
