package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.businessModule.search.BusinessModuleResponse;
import com.kynsof.identity.domain.dto.BusinessModuleDto;
import com.kynsof.identity.domain.dto.ModuleDto;
import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessModuleService;
import com.kynsof.identity.infrastructure.entities.BusinessModule;
import com.kynsof.identity.infrastructure.repository.command.BusinessModuleWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.BusinessModuleReadDataJPARepository;
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

@Service
public class BusinessModuleServiceImpl implements IBusinessModuleService {

    private final BusinessModuleWriteDataJPARepository commandRepository;
    private final BusinessModuleReadDataJPARepository queryRepository;

    public BusinessModuleServiceImpl(BusinessModuleWriteDataJPARepository commandRepository,
                                     BusinessModuleReadDataJPARepository queryRepository) {
        this.commandRepository = commandRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessModuleDto> findBusinessModuleByBusinessId(UUID businessId) {
        return queryRepository.findByBusinessId(businessId).stream()
                .map(businessModule -> new BusinessModuleDto(
                        businessModule.getId(),
                        businessModule.getBusiness().toAggregate(),
                        businessModule.getModule().toAggregateSimple()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleDto> findModulesByBusinessId(UUID businessId) {
        // Usar query con JOIN FETCH para cargar permisos en una sola consulta
        return queryRepository.findModulesWithPermissionsByBusinessId(businessId).stream()
                .map(module -> {
                    var permissionsDto = module.getPermissions().stream()
                            .map(permission -> new PermissionDto(
                                    permission.getId(),
                                    permission.getCode(),
                                    permission.getDescription(),
                                    null,
                                    permission.getStatus(),
                                    permission.getAction(),
                                    permission.getCreatedAt()))
                            .toList();

                    return new ModuleDto(module.getId(), module.getName(), module.getImage(),
                            module.getDescription(), permissionsDto);
                })
                .toList();
    }

    @Override
    public void create(BusinessModuleDto object) {
        commandRepository.save(new BusinessModule(object));
    }

    @Override
    @Transactional
    public void create(List<BusinessModuleDto> objects) {
        var businessModules = objects.stream()
                .map(BusinessModule::new)
                .toList();
        commandRepository.saveAll(businessModules);
    }

    @Override
    @Transactional
    public void update(List<BusinessModuleDto> objects) {
        var businessModules = objects.stream()
                .map(BusinessModule::new)
                .toList();
        commandRepository.saveAll(businessModules);
    }

    @Override
    public void delete(BusinessModuleDto object) {
        commandRepository.delete(new BusinessModule(object));
    }

    @Override
    @Transactional
    public void delete(List<BusinessModuleDto> deletes) {
        var deletesObject = deletes.stream()
                .map(BusinessModule::new)
                .toList();
        commandRepository.deleteAll(deletesObject);
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessModuleDto findById(UUID id) {
        // Usar findByIdWithRelations para cargar business y module con JOIN FETCH
        return queryRepository.findByIdWithRelations(id)
                .map(BusinessModule::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.BUSINESS_MODULE_NOT_FOUND, new ErrorField("id", "BusinessModule not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        var specifications = new GenericSpecificationsBuilder<BusinessModule>(filterCriteria);
        Page<BusinessModule> data = queryRepository.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<BusinessModule> data) {
        var responses = data.getContent().stream()
                .map(businessModule -> new BusinessModuleResponse(businessModule.toAggregate()))
                .toList();
        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByBussinessIdAndModuleId(UUID businessId, UUID moduleId) {
        return queryRepository.countByBussinessIdAndModuleId(businessId, moduleId);
    }
}