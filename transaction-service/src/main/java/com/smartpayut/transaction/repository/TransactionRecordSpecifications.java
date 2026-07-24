package com.smartpayut.transaction.repository;

import java.time.OffsetDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.smartpayut.transaction.domain.entity.TransactionRecord;
import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class TransactionRecordSpecifications {

    private TransactionRecordSpecifications() {
    }

    public static Specification<TransactionRecord> filteredBy(
            String search,
            TransactionType type,
            String method,
            TransactionStatus status,
            OffsetDateTime fromDate,
            OffsetDateTime toDateExclusive) {
        return (root, query, criteriaBuilder) -> {
            Predicate filters = criteriaBuilder.conjunction();

            if (search != null) {
                Expression<String> id = root.get("id").as(String.class);
                Expression<String> userId = root.get("userAccountId").as(String.class);
                Expression<String> walletId = root.get("walletId").as(String.class);

                filters = criteriaBuilder.and(filters, criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(id), search),
                        criteriaBuilder.like(criteriaBuilder.lower(userId), search),
                        criteriaBuilder.like(criteriaBuilder.lower(walletId), search),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("busCode")), search),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("routeName")), search)));
            }

            if (type != null) {
                filters = criteriaBuilder.and(
                        filters,
                        criteriaBuilder.equal(root.get("transactionType"), type));
            }

            if (method != null) {
                filters = criteriaBuilder.and(
                        filters,
                        criteriaBuilder.equal(
                                criteriaBuilder.upper(root.get("method")),
                                method));
            }

            if (status != null) {
                filters = criteriaBuilder.and(
                        filters,
                        criteriaBuilder.equal(root.get("status"), status));
            }

            if (fromDate != null) {
                filters = criteriaBuilder.and(
                        filters,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("occurredAt"),
                                fromDate));
            }

            if (toDateExclusive != null) {
                filters = criteriaBuilder.and(
                        filters,
                        criteriaBuilder.lessThan(
                                root.get("occurredAt"),
                                toDateExclusive));
            }

            return filters;
        };
    }
}
