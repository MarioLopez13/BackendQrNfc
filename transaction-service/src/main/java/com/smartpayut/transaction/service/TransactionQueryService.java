package com.smartpayut.transaction.service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.transaction.domain.enumeration.TransactionStatus;
import com.smartpayut.transaction.domain.enumeration.TransactionType;
import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.dto.response.TransactionResponse;
import com.smartpayut.transaction.exception.TransactionNotFoundException;
import com.smartpayut.transaction.mapper.TransactionMapper;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.repository.TransactionRecordSpecifications;
import com.smartpayut.transaction.validator.PaginationValidator;

@Service
@Transactional(readOnly = true)
public class TransactionQueryService {

    private static final Set<String> SUPPORTED_METHODS = Set.of("QR", "NFC", "PLACETOPAY");

    private final TransactionRecordRepository repository;
    private final TransactionMapper mapper;
    private final PaginationValidator paginationValidator;

    public TransactionQueryService(
            TransactionRecordRepository repository,
            TransactionMapper mapper,
            PaginationValidator paginationValidator) {
        this.repository = repository;
        this.mapper = mapper;
        this.paginationValidator = paginationValidator;
    }

    public PageResponse<TransactionResponse> mine(UUID userId, int page, int pageSize) {
        paginationValidator.validate(page, pageSize);
        Page<TransactionResponse> result = repository
                .findAllByUserAccountIdAndTransactionTypeNot(
                        userId,
                        TransactionType.WALLET_CREATED,
                        pageable(page, pageSize))
                .map(mapper::toResponse);
        return PageResponse.from(result);
    }

    public TransactionResponse mineById(UUID userId, UUID transactionId) {
        return repository.findByIdAndUserAccountId(transactionId, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException("Transacción no encontrada."));
    }

    public PageResponse<TransactionResponse> all(int page, int pageSize) {
        return all(page, pageSize, null, null, null, null, null, null);
    }

    public PageResponse<TransactionResponse> all(
            int page,
            int pageSize,
            String search,
            String type,
            String method,
            String status,
            LocalDate from,
            LocalDate to) {
        paginationValidator.validate(page, pageSize);
        validateDateRange(from, to);

        String normalizedSearch = normalizeSearch(search);
        TransactionType transactionType = enumValue(TransactionType.class, type, "type");
        String normalizedMethod = methodValue(method);
        TransactionStatus transactionStatus = enumValue(TransactionStatus.class, status, "status");
        OffsetDateTime fromDate = from == null ? null : from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toDateExclusive = to == null
                ? null
                : to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return PageResponse.from(repository.findAll(
                TransactionRecordSpecifications.filteredBy(
                        normalizedSearch,
                        transactionType,
                        normalizedMethod,
                        transactionStatus,
                        fromDate,
                        toDateExclusive),
                pageable(page, pageSize))
                .map(mapper::toResponse));
    }

    private PageRequest pageable(int page, int pageSize) {
        return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "occurredAt"));
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from no puede ser posterior a to.");
        }
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private String methodValue(String method) {
        if (method == null || method.isBlank()) {
            return null;
        }

        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED_METHODS.contains(normalized)) {
            throw new IllegalArgumentException(
                    "method debe ser QR, NFC o PLACETOPAY.");
        }
        return normalized;
    }

    private <E extends Enum<E>> E enumValue(
            Class<E> enumType,
            String value,
            String parameter) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    parameter + " contiene un valor no soportado.");
        }
    }
}
