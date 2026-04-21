package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface IAdvertisementImageService {

    UUID create(AdvertisementImageDto dto);

    void update(AdvertisementImageDto dto);

    void delete(UUID id);

    AdvertisementImageDto findById(UUID id);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    List<AdvertisementImageDto> findAll();

    List<AdvertisementImageDto> findByBusinessId(UUID businessId);
}
