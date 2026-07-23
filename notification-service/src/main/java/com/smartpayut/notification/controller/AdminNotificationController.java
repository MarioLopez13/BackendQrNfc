package com.smartpayut.notification.controller;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.dto.common.ApiResponse;
import com.smartpayut.notification.dto.request.NotificationFilter;
import com.smartpayut.notification.dto.response.NotificationResponse;
import com.smartpayut.notification.dto.response.PageResponse;
import com.smartpayut.notification.service.NotificationQueryService;

@RestController
@RequestMapping("/api/admin/notifications")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
public class AdminNotificationController {

    private final NotificationQueryService queryService;

    public AdminNotificationController(NotificationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NotificationResponse>> all(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationSource source,
            @RequestParam(required = false) NotificationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        NotificationFilter filter = new NotificationFilter(userId, type, source, status, dateFrom, dateTo);
        return ApiResponse.ok("Notificaciones administrativas consultadas correctamente.",
                queryService.search(filter, page, pageSize));
    }
}
