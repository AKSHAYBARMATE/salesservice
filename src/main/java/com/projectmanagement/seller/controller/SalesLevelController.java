package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelRequestDto;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelResponseDto;
import com.projectmanagement.seller.service.SalesLevelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/sales-levels")
@RequiredArgsConstructor
public class SalesLevelController {

    private final SalesLevelService salesLevelService;

    @PostMapping("/createSalesLevel")
    public ResponseEntity<StandardResponse<SalesLevelResponseDto>> createSalesLevel(@Valid @RequestBody SalesLevelRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(salesLevelService.createSalesLevel(requestDto));
    }

    @GetMapping("/getAllSalesLevels")
    public ResponseEntity<StandardResponse<List<SalesLevelResponseDto>>> getAllSalesLevels() {
        return ResponseEntity.ok(salesLevelService.getAllSalesLevels());
    }

    @GetMapping("/getSalesLevelById/{id}")
    public ResponseEntity<StandardResponse<SalesLevelResponseDto>> getSalesLevelById(@PathVariable Long id) {
        return ResponseEntity.ok(salesLevelService.getSalesLevelById(id));
    }

    @PutMapping("/updateSalesLevel/{id}")
    public ResponseEntity<StandardResponse<SalesLevelResponseDto>> updateSalesLevel(@PathVariable Long id, @Valid @RequestBody SalesLevelRequestDto requestDto) {
        return ResponseEntity.ok(salesLevelService.updateSalesLevel(id, requestDto));
    }

    @DeleteMapping("/deleteSalesLevel/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteSalesLevel(@PathVariable Long id) {
        return ResponseEntity.ok(salesLevelService.deleteSalesLevel(id));
    }
}
