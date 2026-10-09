package com.projectmanagement.seller.service.impl;

import com.projectmanagement.seller.common.StandardResponse;
import com.projectmanagement.seller.dto.NotificationDtos.NotificationResponseDto;
import com.projectmanagement.seller.entity.Notification;
import com.projectmanagement.seller.entity.User;
import com.projectmanagement.seller.exception.CustomException;
import com.projectmanagement.seller.exception.ResourceNotFoundException;
import com.projectmanagement.seller.repository.NotificationRepository;
import com.projectmanagement.seller.repository.UserRepository;
import com.projectmanagement.seller.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendNotification(Long userId, String title, String message) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            log.info("Notification sent to userId={}: title={}", userId, title);
        } catch (Exception e) {
            log.error("Failed to send notification to userId={}: {}", userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<Page<NotificationResponseDto>> getUserNotifications(Long userId, Pageable pageable) {
        if (userId == null) {
            return StandardResponse.success(Page.empty(), "No user specified");
        }

        Page<NotificationResponseDto> page = notificationRepository.findByUserIdOrderByCreatedOnDesc(userId, pageable)
                .map(this::mapToDto);

        return StandardResponse.success(page, "Notifications fetched successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public StandardResponse<List<NotificationResponseDto>> getUnreadNotifications(Long userId) {
        if (userId == null) {
            return StandardResponse.success(List.of(), "No user specified");
        }

        List<NotificationResponseDto> list = notificationRepository.findByUserIdAndIsReadFalse(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return StandardResponse.success(list, "Unread notifications fetched successfully");
    }

    @Override
    @Transactional
    public StandardResponse<Void> markAsRead(Long notificationId) {
        if (notificationId == null) {
            throw new CustomException("Notification ID cannot be null", "INVALID_INPUT");
        }
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setIsRead(true);
        notificationRepository.save(notification);

        return StandardResponse.success("Notification marked as read");
    }

    @Override
    @Transactional
    public StandardResponse<Void> markAllAsRead(Long userId) {
        if (userId == null) {
            throw new CustomException("User ID cannot be null", "INVALID_INPUT");
        }
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalse(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);

        return StandardResponse.success("All notifications marked as read");
    }

    private NotificationResponseDto mapToDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdOn(notification.getCreatedOn())
                .build();
    }
}
