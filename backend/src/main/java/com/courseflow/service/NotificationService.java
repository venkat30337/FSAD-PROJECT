package com.courseflow.service;

import com.courseflow.dto.response.NotificationResponse;
import com.courseflow.exception.ResourceNotFoundException;
import com.courseflow.model.Notification;
import com.courseflow.model.NotificationType;
import com.courseflow.model.User;
import com.courseflow.repository.NotificationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void create(User user, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
            .user(user)
            .title(title)
            .message(message)
            .type(type)
            .isRead(false)
            .build();
        notificationRepository.save(notification);
    }

    public Page<NotificationResponse> listByUser(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map(this::toResponse);
    }

    public List<NotificationResponse> latestByUser(Long userId) {
        return notificationRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setIsRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public int markAllRead(Long userId) {
        return notificationRepository.markAllRead(userId);
    }

    public long unreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .title(notification.getTitle())
            .message(notification.getMessage())
            .type(notification.getType())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();
    }
}
