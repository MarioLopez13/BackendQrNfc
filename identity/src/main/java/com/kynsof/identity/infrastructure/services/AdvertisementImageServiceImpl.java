package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.advertisementImage.search.AdvertisementImageResponse;
import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.interfaces.service.IAdvertisementImageService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.identity.infrastructure.entities.AdvertisementImage;
import com.kynsof.identity.infrastructure.repository.command.AdvertisementImageWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.AdvertisementImageReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdvertisementImageServiceImpl implements IAdvertisementImageService {

    private final AdvertisementImageWriteDataJPARepository repositoryCommand;
    private final AdvertisementImageReadDataJPARepository repositoryQuery;

    public AdvertisementImageServiceImpl(AdvertisementImageWriteDataJPARepository repositoryCommand,
                                          AdvertisementImageReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = {
                    IdentityCacheConfig.ADVERTISEMENT_IMAGE_CACHE
            }, allEntries = true)
    })
    public UUID create(AdvertisementImageDto dto) {
        AdvertisementImage entity = new AdvertisementImage(dto);
        AdvertisementImage savedEntity = this.repositoryCommand.save(entity);
        return savedEntity.getId();
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = {
                    IdentityCacheConfig.ADVERTISEMENT_IMAGE_CACHE
            }, allEntries = true)
    })
    public void update(AdvertisementImageDto dto) {
        AdvertisementImage entity = new AdvertisementImage(dto);
        this.repositoryCommand.save(entity);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = {
                    IdentityCacheConfig.ADVERTISEMENT_IMAGE_CACHE
            }, allEntries = true)
    })
    public void delete(UUID id) {
        try {
            this.repositoryCommand.deleteById(id);
        } catch (Exception e) {
            throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.NOT_DELETE,
                    new ErrorField("id", "Advertisement Image cannot be deleted, it may have related elements.")));
        }
    }

    @Override
    @Cacheable(value = IdentityCacheConfig.ADVERTISEMENT_IMAGE_CACHE, key = "#id", unless = "#result == null")
    public AdvertisementImageDto findById(UUID id) {
        Optional<AdvertisementImage> object = this.repositoryQuery.findById(id);
        if (object.isPresent()) {
            return object.get().toAggregate();
        }
        throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.BUSINESS_NOT_FOUND,
                new ErrorField("id", "Advertisement Image not found.")));
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<AdvertisementImage> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<AdvertisementImage> data = this.repositoryQuery.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<AdvertisementImage> data) {
        List<AdvertisementImageResponse> responses = new ArrayList<>();
        for (AdvertisementImage entity : data.getContent()) {
            responses.add(new AdvertisementImageResponse(entity.toAggregate()));
        }
        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    public List<AdvertisementImageDto> findAll() {
        return this.repositoryQuery.findAll().stream()
                .map(AdvertisementImage::toAggregate)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = IdentityCacheConfig.ADVERTISEMENT_IMAGE_CACHE, key = "'business:' + #businessId", unless = "#result == null || #result.isEmpty()")
    public List<AdvertisementImageDto> findByBusinessId(UUID businessId) {
        return this.repositoryQuery.findByBusinessId(businessId).stream()
                .map(AdvertisementImage::toAggregate)
                .collect(Collectors.toList());
    }
}
