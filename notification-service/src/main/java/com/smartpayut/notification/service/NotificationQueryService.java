package com.smartpayut.notification.service;

import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.notification.domain.enumeration.NotificationSource;
import com.smartpayut.notification.domain.enumeration.NotificationStatus;
import com.smartpayut.notification.domain.enumeration.NotificationType;
import com.smartpayut.notification.dto.request.NotificationFilter;
import com.smartpayut.notification.dto.response.NotificationResponse;
import com.smartpayut.notification.dto.response.PageResponse;
import com.smartpayut.notification.dto.response.UnreadCountResponse;
import com.smartpayut.notification.exception.NotificationNotFoundException;
import com.smartpayut.notification.mapper.NotificationMapper;
import com.smartpayut.notification.repository.NotificationRepository;
import com.smartpayut.notification.validator.PaginationValidator;

@Service
@Transactional(readOnly = true)
public class NotificationQueryService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;
    private final PaginationValidator paginationValidator;

    public NotificationQueryService(
            NotificationRepository repository,
            NotificationMapper mapper,
            PaginationValidator paginationValidator) {
        this.repository = repository;
        this.mapper = mapper;
        this.paginationValidator = paginationValidator;
    }

    public PageResponse<NotificationResponse> mine(
            UUID userId,
            NotificationStatus status,
            NotificationSource source,
            NotificationType type,
            int page,
            int pageSize) {
        NotificationFilter filter = new NotificationFilter(userId, type, source, status, null, null);
        return search(filter, page, pageSize);
    }

    public NotificationResponse mineById(UUID userId, UUID notificationId) {
        return repository.findByIdAndUserId(notificationId, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException("Notificación no encontrada."));
    }

    public UnreadCountResponse unreadCount(UUID userId) {
        return new UnreadCountResponse(repository.countByUserIdAndStatus(userId, NotificationStatus.UNREAD));
    }

    public PageResponse<NotificationResponse> search(NotificationFilter filter, int page, int pageSize) {
        paginationValidator.validate(page, pageSize);
        PageRequest pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(repository.findAll(NotificationSpecifications.from(filter), pageable)
                .map(mapper::toResponse));
    }
}
