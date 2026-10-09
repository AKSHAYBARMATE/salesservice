package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuthDtos.*;
import com.projectmanagement.seller.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sellerservice")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping({"/login", "/auth/login"})
    public ResponseEntity<StandardResponse<LoginResponseDto>> login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.login(requestDto, request));
    }

    @PostMapping({"/forgotPassword", "/auth/forgot-password", "/auth/forgotPassword"})
    public ResponseEntity<StandardResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.forgotPassword(requestDto));
    }

    @PostMapping({"/verifyOtp", "/auth/verify-otp", "/auth/verifyOtp"})
    public ResponseEntity<StandardResponse<Void>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.verifyOtp(requestDto));
    }

    @PostMapping({"/resetPassword", "/auth/reset-password", "/auth/resetPassword"})
    public ResponseEntity<StandardResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDto requestDto
    ) {
        return ResponseEntity.ok(authService.resetPassword(requestDto));
    }
}
