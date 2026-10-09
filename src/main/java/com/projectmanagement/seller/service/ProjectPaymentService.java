package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentRequestDto;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentResponseDto;

import java.util.List;

public interface ProjectPaymentService {
    StandardResponse<PaymentResponseDto> createPayment(Long projectId, PaymentRequestDto requestDto);
    StandardResponse<List<PaymentResponseDto>> getPaymentsByProject(Long projectId);
}
