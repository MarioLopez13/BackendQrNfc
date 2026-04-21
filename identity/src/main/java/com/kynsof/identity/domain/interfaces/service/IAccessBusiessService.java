package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAccessBusiessService {
    UUID create(AccessBusinessDto object);

    void update(AccessBusinessDto object);

    void delete(UUID id);

    AccessBusinessDto findById(UUID id);

    AccessBusinessDto getById(UUID id);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    List<AccessBusinessDto> findAll();
}
