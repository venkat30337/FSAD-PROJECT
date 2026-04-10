package com.courseflow.controller;

import com.courseflow.dto.response.NotificationResponse;
import com.courseflow.model.User;
import com.courseflow.service.NotificationService;
import com.courseflow.service.UserService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<Page<NotificationResponse>> notifications(Authentication authentication,
                                                                    @PageableDefault(size = 10) Pageable pageable) {
        User user = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(notificationService.listByUser(user.getId(), pageable));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<NotificationResponse>> latest(Authentication authentication) {
        User user = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(notificationService.latestByUser(user.getId()));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markRead(Authentication authentication, @PathVariable Long id) {
        User user = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(notificationService.markRead(user.getId(), id));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Map<String, Integer>> markAllRead(Authentication authentication) {
        User user = userService.requireByEmail(authentication.getName());
        int updated = notificationService.markAllRead(user.getId());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(Authentication authentication) {
        User user = userService.requireByEmail(authentication.getName());
        return ResponseEntity.ok(Map.of("count", notificationService.unreadCount(user.getId())));
    }
}
