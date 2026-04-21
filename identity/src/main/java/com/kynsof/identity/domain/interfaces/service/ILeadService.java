package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ILeadService {

    UUID create(LeadDto dto);

    void update(LeadDto dto);

    void delete(UUID id);

    LeadDto findById(UUID id);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    boolean existsByEmail(String email);

    void updateStatus(UUID id, ELeadStatus status, String notes);

    Long countByStatus(ELeadStatus status);

    Long countActiveLeads();
}
