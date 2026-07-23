package com.smartpayut.notification.service;

import org.springframework.data.jpa.domain.Specification;

import com.smartpayut.notification.domain.entity.Notification;
import com.smartpayut.notification.dto.request.NotificationFilter;

final class NotificationSpecifications {

    private NotificationSpecifications() {
    }

    static Specification<Notification> from(NotificationFilter filter) {
        Specification<Notification> specification = Specification.where(null);
        if (filter.userId() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("userId"), filter.userId()));
        }
        if (filter.type() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("type"), filter.type()));
        }
        if (filter.source() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("source"), filter.source()));
        }
        if (filter.status() != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
        }
        if (filter.dateFrom() != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), filter.dateFrom()));
        }
        if (filter.dateTo() != null) {
            specification = specification.and(
                    (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), filter.dateTo()));
        }
        return specification;
    }
}
