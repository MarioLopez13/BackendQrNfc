package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.identity.domain.interfaces.service.ILeadService;
import com.kynsof.identity.infrastructure.entities.Lead;
import com.kynsof.identity.infrastructure.repository.command.LeadWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.LeadReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LeadServiceImpl implements ILeadService {

    private final LeadWriteDataJPARepository repositoryCommand;
    private final LeadReadDataJPARepository repositoryQuery;

    public LeadServiceImpl(LeadWriteDataJPARepository repositoryCommand,
                           LeadReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    @Transactional
    public UUID create(LeadDto dto) {
        Lead entity = new Lead(dto);
        Lead saved = repositoryCommand.save(entity);
        return saved.getId();
    }

    @Override
    @Transactional
    public void update(LeadDto dto) {
        repositoryCommand.save(new Lead(dto));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repositoryCommand.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadDto findById(UUID id) {
        return repositoryQuery.findById(id)
                .map(Lead::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(
                        new GlobalBusinessException(DomainErrorMessage.LEAD_NOT_FOUND,
                                new ErrorField("id", "Lead not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        filterCriteria.forEach(filter -> {
            if ("status".equals(filter.getKey()) && filter.getValue() instanceof String) {
                filter.setValue(ELeadStatus.valueOf((String) filter.getValue()));
            }
        });

        GenericSpecificationsBuilder<Lead> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<Lead> page = repositoryQuery.findAll(specifications, pageable);

        List<LeadDto> responses = page.getContent().stream()
                .map(Lead::toAggregate)
                .collect(Collectors.toList());

        return new PaginatedResponse(
                responses,
                page.getTotalPages(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getSize(),
                page.getNumber()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return repositoryQuery.existsByEmail(email);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, ELeadStatus status, String notes) {
        Lead lead = repositoryQuery.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException(
                        new GlobalBusinessException(DomainErrorMessage.LEAD_NOT_FOUND,
                                new ErrorField("id", "Lead not found."))));

        lead.setStatus(status);
        if (notes != null && !notes.isEmpty()) {
            String existingNotes = lead.getNotes() != null ? lead.getNotes() + "\n" : "";
            lead.setNotes(existingNotes + notes);
        }
        repositoryCommand.save(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByStatus(ELeadStatus status) {
        return repositoryQuery.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countActiveLeads() {
        return repositoryQuery.countActiveLEads();
    }
}
