package com.yasarbilgi.visitormeetingmanagment.notification.controller;

import com.yasarbilgi.visitormeetingmanagment.security.model.AuthenticatedUser;
import com.yasarbilgi.visitormeetingmanagment.common.response.ApiResponse;
import com.yasarbilgi.visitormeetingmanagment.common.response.PageResponse;
import com.yasarbilgi.visitormeetingmanagment.notification.dto.response.NotificationResponseDto;
import com.yasarbilgi.visitormeetingmanagment.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponseDto>>> getMyNotifications(
            @PathVariable Long companyId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        PageResponse<NotificationResponseDto> notifications = PageResponse.of(
                notificationService.listForUser(currentUser.userId(), pageable)
                        .map(NotificationResponseDto::from)
        );

        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @PathVariable Long companyId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        long count = notificationService.countUnread(currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long companyId,
            @PathVariable Long notificationId,
            @AuthenticationPrincipal AuthenticatedUser currentUser
    ) {
        notificationService.markAsRead(notificationId, currentUser.userId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read"));
    }
}