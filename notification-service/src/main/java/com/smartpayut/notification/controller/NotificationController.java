package com.smartpayut.notification.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.dto.common.ApiResponse;
import com.smartpayut.notification.dto.response.NotificationResponse;
import com.smartpayut.notification.dto.response.PageResponse;
import com.smartpayut.notification.dto.response.UnreadCountResponse;
import com.smartpayut.notification.security.CurrentUserIdResolver;
import com.smartpayut.notification.service.NotificationCommandService;
import com.smartpayut.notification.service.NotificationQueryService;

@RestController
@RequestMapping("/api/notifications/me")
public class NotificationController {

    private final NotificationQueryService queryService;
    private final NotificationCommandService commandService;
    private final CurrentUserIdResolver userIdResolver;

    public NotificationController(
            NotificationQueryService queryService,
            NotificationCommandService commandService,
            CurrentUserIdResolver userIdResolver) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.userIdResolver = userIdResolver;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> mine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) NotificationSource source,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ApiResponse.ok("Notificaciones consultadas correctamente.",
                queryService.mine(userIdResolver.resolve(jwt), status, source, type, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<NotificationResponse> byId(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return ApiResponse.ok("Notificación consultada correctamente.",
                queryService.mineById(userIdResolver.resolve(jwt), id));
    }

    @GetMapping("/unread-count")
    public ApiResponse<UnreadCountResponse> unreadCount(@AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.ok("Notificaciones no leídas consultadas correctamente.",
                queryService.unreadCount(userIdResolver.resolve(jwt)));
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<NotificationResponse> markAsRead(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id) {
        return ApiResponse.ok("Notificación marcada como leída.",
                commandService.markAsRead(userIdResolver.resolve(jwt), id));
    }

    @PatchMapping("/read-all")
    public ApiResponse<Map<String, Long>> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        long count = commandService.markAllAsRead(userIdResolver.resolve(jwt));
        return ApiResponse.ok("Notificaciones marcadas como leídas.", Map.of("updatedCount", count));
    }
}
