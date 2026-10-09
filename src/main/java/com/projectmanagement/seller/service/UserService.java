package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.UserDtos.UserCreateRequestDto;
import com.projectmanagement.seller.dto.UserDtos.UserResponseDto;
import com.projectmanagement.seller.dto.UserDtos.UserStatusUpdateRequestDto;
import com.projectmanagement.seller.dto.UserDtos.UserUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    StandardResponse<UserResponseDto> createUser(UserCreateRequestDto requestDto);
    StandardResponse<Page<UserResponseDto>> getUsers(String search, Long roleId, Long levelId, String status, Pageable pageable);
    StandardResponse<UserResponseDto> getUserById(Long id);
    StandardResponse<UserResponseDto> updateUser(Long id, UserUpdateRequestDto requestDto);
    StandardResponse<UserResponseDto> updateUserStatus(Long id, UserStatusUpdateRequestDto requestDto);
    StandardResponse<Void> deleteUser(Long id);
}
