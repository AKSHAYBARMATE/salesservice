package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentRequestDto;
import com.projectmanagement.seller.dto.PaymentDtos.PaymentResponseDto;
import com.projectmanagement.seller.service.ProjectPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/payments")
@RequiredArgsConstructor
public class ProjectPaymentController {

    private final ProjectPaymentService paymentService;

    @PostMapping("/createPayment/{projectId}")
    public ResponseEntity<StandardResponse<PaymentResponseDto>> createPayment(
            @PathVariable Long projectId,
            @Valid @RequestBody PaymentRequestDto requestDto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(projectId, requestDto));
    }

    @GetMapping("/getPaymentsByProjectId/{projectId}")
    public ResponseEntity<StandardResponse<List<PaymentResponseDto>>> getPaymentsByProjectId(@PathVariable Long projectId) {
        return ResponseEntity.ok(paymentService.getPaymentsByProject(projectId));
    }
}
