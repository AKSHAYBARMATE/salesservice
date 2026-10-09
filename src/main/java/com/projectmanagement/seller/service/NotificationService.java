package com.projectmanagement.seller.service;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.NotificationDtos.NotificationResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    void sendNotification(Long userId, String title, String message);
    StandardResponse<Page<NotificationResponseDto>> getUserNotifications(Long userId, Pageable pageable);
    StandardResponse<List<NotificationResponseDto>> getUnreadNotifications(Long userId);
    StandardResponse<Void> markAsRead(Long notificationId);
    StandardResponse<Void> markAllAsRead(Long userId);
}
