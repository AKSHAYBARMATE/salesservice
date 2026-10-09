package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelRequestDto;
import com.projectmanagement.seller.dto.SalesLevelDtos.SalesLevelResponseDto;

import java.util.List;

public interface SalesLevelService {
    StandardResponse<SalesLevelResponseDto> createSalesLevel(SalesLevelRequestDto requestDto);
    StandardResponse<List<SalesLevelResponseDto>> getAllSalesLevels();
    StandardResponse<SalesLevelResponseDto> getSalesLevelById(Long id);
    StandardResponse<SalesLevelResponseDto> updateSalesLevel(Long id, SalesLevelRequestDto requestDto);
    StandardResponse<Void> deleteSalesLevel(Long id);
}
