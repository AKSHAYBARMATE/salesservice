package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.UserDtos.*;
import com.projectmanagement.seller.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sellerservice/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/createUser")
    public ResponseEntity<StandardResponse<UserResponseDto>> createUser(@Valid @RequestBody UserCreateRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(requestDto));
    }

    @GetMapping("/getUsers")
    public ResponseEntity<StandardResponse<Page<UserResponseDto>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdOn") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(userService.getUsers(search, roleId, levelId, status, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/getUserById/{id}")
    public ResponseEntity<StandardResponse<UserResponseDto>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/updateUser/{id}")
    public ResponseEntity<StandardResponse<UserResponseDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(id, requestDto));
    }

    @PatchMapping("/updateUserStatus/{id}")
    public ResponseEntity<StandardResponse<UserResponseDto>> updateUserStatus(@PathVariable Long id, @Valid @RequestBody UserStatusUpdateRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUserStatus(id, requestDto));
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }
}
