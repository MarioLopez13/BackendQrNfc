package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.interfaces.service.IBusinessConfigurationService;
import com.kynsof.identity.infrastructure.entities.Business;
import com.kynsof.identity.infrastructure.entities.BusinessConfiguration;
import com.kynsof.identity.infrastructure.repository.command.BusinessConfigurationWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.BusinessConfigurationReadDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.BusinessReadDataJPARepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementación del servicio de BusinessConfiguration.
 * Gestiona la lógica de negocio y persistencia de configuraciones.
 */
@Service
public class BusinessConfigurationServiceImpl implements IBusinessConfigurationService {

    private final BusinessConfigurationWriteDataJPARepository repositoryCommand;
    private final BusinessConfigurationReadDataJPARepository repositoryQuery;
    private final BusinessReadDataJPARepository businessRepository;

    public BusinessConfigurationServiceImpl(
            BusinessConfigurationWriteDataJPARepository repositoryCommand,
            BusinessConfigurationReadDataJPARepository repositoryQuery,
            BusinessReadDataJPARepository businessRepository) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
        this.businessRepository = businessRepository;
    }

    @Override
    @Transactional
    public void create(BusinessConfigurationDto dto) {
        // Obtener la entidad Business
        Business business = businessRepository.findById(dto.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException(
                        new GlobalBusinessException(
                                DomainErrorMessage.BUSINESS_NOT_FOUND,
                                new ErrorField("businessId", "Business not found."))));

        BusinessConfiguration entity = new BusinessConfiguration(dto);
        entity.setBusiness(business);
        repositoryCommand.save(entity);
    }

    @Override
    @Transactional
    public void update(BusinessConfigurationDto dto) {
        BusinessConfiguration entity = repositoryQuery.findById(dto.getId())
                .orElseThrow(() -> new BusinessNotFoundException(
                        new GlobalBusinessException(
                                DomainErrorMessage.OBJECT_NOT_FOUNT,
                                new ErrorField("id", "Business Configuration not found."))));

        entity.update(dto);
        entity.setUpdatedAt(LocalDateTime.now());
        repositoryCommand.save(entity);
    }

    @Override
    @Transactional
    public void delete(BusinessConfigurationDto dto) {
        BusinessConfiguration entity = repositoryQuery.findById(dto.getId())
                .orElseThrow(() -> new BusinessNotFoundException(
                        new GlobalBusinessException(
                                DomainErrorMessage.OBJECT_NOT_FOUNT,
                                new ErrorField("id", "Business Configuration not found."))));

        entity.deactivate();
        repositoryCommand.save(entity);
    }

    @Override
    public BusinessConfigurationDto findById(UUID id) {
        Optional<BusinessConfiguration> entity = repositoryQuery.findById(id);
        if (entity.isPresent()) {
            return entity.get().toAggregate();
        }
        throw new BusinessNotFoundException(
                new GlobalBusinessException(
                        DomainErrorMessage.OBJECT_NOT_FOUNT,
                        new ErrorField("id", "Business Configuration not found.")));
    }

    @Override
    public BusinessConfigurationDto findByBusinessIdAndConfigKey(UUID businessId, String configKey) {
        Optional<BusinessConfiguration> entity = repositoryQuery.findByBusinessIdAndConfigKey(businessId, configKey);
        if (entity.isPresent()) {
            return entity.get().toAggregate();
        }
        throw new BusinessNotFoundException(
                new GlobalBusinessException(
                        DomainErrorMessage.OBJECT_NOT_FOUNT,
                        new ErrorField("configKey", "Configuration not found for the given business and key.")));
    }

    @Override
    public BusinessConfigurationDto findByConfigKey(String configKey) {
        Optional<BusinessConfiguration> entity = repositoryQuery.findByConfigKey(configKey);
        if (entity.isPresent()) {
            return entity.get().toAggregate();
        }
        throw new BusinessNotFoundException(
                new GlobalBusinessException(
                        DomainErrorMessage.OBJECT_NOT_FOUNT,
                        new ErrorField("configKey", "Configuration not found for the given key.")));
    }

    @Override
    public List<BusinessConfigurationDto> findAllByBusinessId(UUID businessId) {
        return repositoryQuery.findAllByBusinessId(businessId).stream()
                .map(BusinessConfiguration::toAggregate)
                .toList();
    }

    @Override
    public List<BusinessConfigurationDto> findByBusinessIdAndCategory(UUID businessId, EConfigCategory category) {
        return repositoryQuery.findByBusinessIdAndCategory(businessId, category).stream()
                .map(BusinessConfiguration::toAggregate)
                .toList();
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<BusinessConfiguration> specifications =
                new GenericSpecificationsBuilder<>(filterCriteria);
        Page<BusinessConfiguration> data = repositoryQuery.findAll(specifications, pageable);

        return new PaginatedResponse(
                data.getContent().stream()
                        .map(BusinessConfiguration::toAggregate)
                        .toList(),
                data.getTotalPages(),
                data.getNumberOfElements(),
                data.getTotalElements(),
                data.getSize(),
                data.getNumber()
        );
    }

    @Override
    public Long countByBusinessIdAndConfigKeyAndNotId(UUID businessId, String configKey, UUID id) {
        return repositoryQuery.countByBusinessIdAndConfigKeyAndNotId(businessId, configKey, id);
    }

    @Override
    public Long countByBusinessIdAndConfigKey(UUID businessId, String configKey) {
        return repositoryQuery.countByBusinessIdAndConfigKey(businessId, configKey);
    }
}
