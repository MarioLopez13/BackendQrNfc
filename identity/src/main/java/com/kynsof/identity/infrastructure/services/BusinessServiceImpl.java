package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.business.search.BusinessResponse;
import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.identity.infrastructure.entities.Business;
import com.kynsof.identity.infrastructure.repository.command.BusinessWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.BusinessReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import com.kynsof.share.utils.ConfigureTimeZone;
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
public class BusinessServiceImpl implements IBusinessService {

    private final BusinessWriteDataJPARepository repositoryCommand;
    private final BusinessReadDataJPARepository repositoryQuery;

    public BusinessServiceImpl(BusinessWriteDataJPARepository repositoryCommand,
                             BusinessReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = {
//                    IdentityCacheConfig.BUSINESS_CACHE
//            }, allEntries = true)
//    })
    public UUID create(BusinessDto object) {
        object.setStatus(EBusinessStatus.ACTIVE);
        Business businessEntity = new Business(object);
        Business savedEntity = this.repositoryCommand.save(businessEntity);
        return savedEntity.getId();
    }

    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = {
//                    IdentityCacheConfig.BUSINESS_CACHE
//            }, allEntries = true)
//    })
    public void update(BusinessDto objectDto) {
        Business update = new Business(objectDto);
        update.setUpdatedAt(ConfigureTimeZone.getTimeZone());
        this.repositoryCommand.save(update);
    }

    @Override
    @Transactional
//    @Caching(evict = {
//            @CacheEvict(value = {
//                    IdentityCacheConfig.BUSINESS_CACHE
//            }, allEntries = true)
//    })
    public void delete(UUID id) {
        try {
            this.repositoryCommand.deleteById(id);
        } catch (Exception e) {
            throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.NOT_DELETE,
                    new ErrorField("id", "Element cannot be deleted has a related element.")));
        }
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = IdentityCacheConfig.BUSINESS_CACHE, key = "#id", unless = "#result == null")
    public BusinessDto findById(UUID id) {
        return this.repositoryQuery.findByIdWithGeographicLocation(id)
                .map(Business::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND, new ErrorField("id", "Business not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessDto getById(UUID id) {
        return this.repositoryQuery.findByIdWithGeographicLocation(id)
                .map(Business::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_NOT_FOUND, new ErrorField("id", "Business not found."))));
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = IdentityCacheConfig.BUSINESS_CACHE,
//            key = "'search:' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort + ':' + T(java.util.Objects).hash(#filterCriteria)",
//            unless = "#result == null")
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        filterCreteria(filterCriteria);
        GenericSpecificationsBuilder<Business> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<Business> data = this.repositoryQuery.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private void filterCreteria(List<FilterCriteria> filterCriteria) {
        for (FilterCriteria filter : filterCriteria) {
            if ("status".equals(filter.getKey()) && filter.getValue() instanceof String) {
                try {
                    EBusinessStatus enumValue = EBusinessStatus.valueOf((String) filter.getValue());
                    filter.setValue(enumValue);
                } catch (IllegalArgumentException e) {
                    System.err.println("Valor inválido para el tipo Enum Empresa: " + filter.getValue());
                }
            }
        }
    }

    private PaginatedResponse getPaginatedResponse(Page<Business> data) {
        List<BusinessResponse> patients = new ArrayList<>();
        for (Business o : data.getContent()) {
            patients.add(new BusinessResponse(o.toAggregate()));
        }
        return new PaginatedResponse(patients, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    public Long countByName(String name) {
        return repositoryQuery.countByName(name);
    }

    @Override
    public Long countByRuc(String ruc) {
        return repositoryQuery.countByRuc(ruc);
    }

    @Override
    public Long countByRucAndNotId(String ruc, UUID id) {
        return repositoryQuery.countByRucAndNotId(ruc, id);
    }

    @Override
    public Long countByNameAndNotId(String name, UUID id) {
        return repositoryQuery.countByNameAndNotId(name, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessDto> findAll() {
        return this.repositoryQuery.findAllWithGeographicLocation().stream()
                .map(Business::toAggregate)
                .collect(Collectors.toList());
    }

}
