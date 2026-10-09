package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.AuthDtos.*;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    StandardResponse<LoginResponseDto> login(LoginRequestDto requestDto, HttpServletRequest request);
    StandardResponse<Void> forgotPassword(ForgotPasswordRequestDto requestDto);
    StandardResponse<Void> verifyOtp(VerifyOtpRequestDto requestDto);
    StandardResponse<Void> resetPassword(ResetPasswordRequestDto requestDto);
}
