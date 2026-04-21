package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.module.getbyid.ModuleResponse;
import com.kynsof.identity.application.query.module.search.ModuleListResponse;
import com.kynsof.identity.domain.dto.ModuleDto;
import com.kynsof.identity.domain.dto.moduleDto.ModuleDataDto;
import com.kynsof.identity.domain.dto.moduleDto.ModuleNodeDto;
import com.kynsof.identity.domain.interfaces.service.IModuleService;
import com.kynsof.identity.infrastructure.config.IdentityCacheConfig;
import com.kynsof.identity.infrastructure.entities.ModuleSystem;
import com.kynsof.identity.infrastructure.repository.command.ModuleWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.ModuleReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ModuleServiceImpl implements IModuleService {

    private final ModuleWriteDataJPARepository repositoryCommand;
    private final ModuleReadDataJPARepository repositoryQuery;

    public ModuleServiceImpl(ModuleWriteDataJPARepository repositoryCommand,
                             ModuleReadDataJPARepository repositoryQuery) {
        this.repositoryCommand = repositoryCommand;
        this.repositoryQuery = repositoryQuery;
    }

    @Override
    @Transactional
    public void create(ModuleDto object) {
        ModuleSystem moduleEntity = new ModuleSystem(object);
        this.repositoryCommand.save(moduleEntity);
    }

    @Override
    public void update(ModuleDto objectDto) {
        ModuleSystem update = new ModuleSystem(objectDto);
        update.setUpdatedAt(LocalDateTime.now());
        this.repositoryCommand.save(update);
    }

    @Override
    public void delete(ModuleDto moduleDto) {
        try {
            this.repositoryCommand.deleteById(moduleDto.getId());
        } catch (Exception e) {
            throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.NOT_DELETE,
                    new ErrorField("id", "Element cannot be deleted has a related element.")));
        }
    }

    @Override
    @Transactional
    public void deleteAll(List<UUID> modules) {
        modules.stream()
                .map(this::findById)
                .map(this::createDeactivatedModule)
                .forEach(repositoryCommand::save);
    }

    private ModuleSystem createDeactivatedModule(ModuleDto moduleDto) {
        var moduleSystem = new ModuleSystem(moduleDto);
        moduleSystem.setName(moduleDto.getName() + "-" + UUID.randomUUID());
        return moduleSystem;
    }

    @Override
    public ModuleDto findById(UUID id) {
        // Usar findByIdWithPermissions para cargar permisos con JOIN FETCH
        return this.repositoryQuery.findByIdWithPermissions(id)
                .map(ModuleSystem::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.MODULE_NOT_FOUND, new ErrorField("id", "Module not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        var specifications = new GenericSpecificationsBuilder<ModuleResponse>(filterCriteria);
        var data = repositoryQuery.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<ModuleSystem> data) {
        // Usar toAggregateSimple() para evitar cargar permisos (LAZY)
        // ModuleListResponse no necesita los permisos
        var moduleListResponses = data.getContent().stream()
                .map(module -> new ModuleListResponse(module.toAggregateSimple()))
                .toList();
        return new PaginatedResponse(moduleListResponses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    public List<ModuleNodeDto> buildStructure() {
        // Usar findAllWithPermissions para cargar módulos con permisos en una sola query (JOIN FETCH)
        List<ModuleSystem> modules = repositoryQuery.findAllWithPermissions();

        log.debug("Construyendo estructura de módulos: {} registros cargados", modules.size());

        return modules.stream()
                .map(this::createModuleNode)
                .toList();
    }

    private ModuleNodeDto createModuleNode(ModuleSystem module) {
        var moduleNode = new ModuleNodeDto();
        moduleNode.setKey(module.getId().toString());
        moduleNode.setData(new ModuleDataDto(module.getName(), "Module", module.getName()));

        var permissionsNodes = module.getPermissions().stream()
                .map(permission -> {
                    var permissionNode = new ModuleNodeDto();
                    permissionNode.setKey(permission.getId().toString());
                    permissionNode.setData(new ModuleDataDto(permission.getDescription(), "Permission", permission.getCode()));
                    return permissionNode;
                }).toList();

        moduleNode.setChildren(permissionsNodes);
        return moduleNode;
    }

    @Override
    public Long countByNameAndNotId(String name, UUID id) {
        return repositoryQuery.countByNameAndNotId(name, id);
    }
}