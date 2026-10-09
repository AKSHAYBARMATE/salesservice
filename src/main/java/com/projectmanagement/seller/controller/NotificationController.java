package com.projectmanagement.seller.controller;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.config.LoginUser;
import com.projectmanagement.seller.dto.NotificationDtos.NotificationResponseDto;
import com.projectmanagement.seller.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sellerservice/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final LoginUser loginUser;

    @GetMapping("/getUserNotifications")
    public ResponseEntity<StandardResponse<Page<NotificationResponseDto>>> getUserNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long targetUserId = (userId != null) ? userId : loginUser.getUserId();
        return ResponseEntity.ok(notificationService.getUserNotifications(targetUserId, PageRequest.of(page, size)));
    }

    @GetMapping("/getUnreadNotifications")
    public ResponseEntity<StandardResponse<List<NotificationResponseDto>>> getUnreadNotifications(
            @RequestParam(required = false) Long userId
    ) {
        Long targetUserId = (userId != null) ? userId : loginUser.getUserId();
        return ResponseEntity.ok(notificationService.getUnreadNotifications(targetUserId));
    }

    @PatchMapping("/markNotificationAsRead/{id}")
    public ResponseEntity<StandardResponse<Void>> markNotificationAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PatchMapping("/markAllNotificationsAsRead")
    public ResponseEntity<StandardResponse<Void>> markAllNotificationsAsRead(@RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : loginUser.getUserId();
        return ResponseEntity.ok(notificationService.markAllAsRead(targetUserId));
    }
}
