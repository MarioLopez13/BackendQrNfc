package com.smartpayut.transaction.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpayut.transaction.dto.response.PageResponse;
import com.smartpayut.transaction.dto.response.TransactionResponse;
import com.smartpayut.transaction.exception.TransactionNotFoundException;
import com.smartpayut.transaction.mapper.TransactionMapper;
import com.smartpayut.transaction.repository.TransactionRecordRepository;
import com.smartpayut.transaction.validator.PaginationValidator;

@Service
@Transactional(readOnly = true)
public class TransactionQueryService {

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
                .findAllByUserAccountId(userId, pageable(page, pageSize))
                .map(mapper::toResponse);
        return PageResponse.from(result);
    }

    public TransactionResponse mineById(UUID userId, UUID transactionId) {
        return repository.findByIdAndUserAccountId(transactionId, userId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException("Transacción no encontrada."));
    }

    public PageResponse<TransactionResponse> all(int page, int pageSize) {
        paginationValidator.validate(page, pageSize);
        return PageResponse.from(repository.findAll(pageable(page, pageSize)).map(mapper::toResponse));
    }

    private PageRequest pageable(int page, int pageSize) {
        return PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "occurredAt"));
    }
}
