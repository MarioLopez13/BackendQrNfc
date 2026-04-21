package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.permission.search.PermissionSearchResponse;
import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.enumType.PermissionStatusEnm;
import com.kynsof.identity.domain.interfaces.service.IPermissionService;
import com.kynsof.identity.infrastructure.entities.Permission;
import com.kynsof.identity.infrastructure.repository.command.PermissionWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.PermissionReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PermissionServiceImpl implements IPermissionService {

    private final PermissionWriteDataJPARepository writeRepository;
    private final PermissionReadDataJPARepository queryRepository;

    @Autowired
    public PermissionServiceImpl(PermissionWriteDataJPARepository writeRepository, PermissionReadDataJPARepository queryRepository) {
        this.writeRepository = writeRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public void create(PermissionDto dto) {
        writeRepository.save(new Permission(dto));
    }

    @Override
    @Transactional
    public void update(PermissionDto objectDto) {
        var update = new Permission(objectDto);
        update.setUpdatedAt(LocalDateTime.now());
        writeRepository.save(update);
    }

    @Override
    @Transactional
    public void delete(PermissionDto objectDto) {
        writeRepository.save(new Permission(objectDto));
    }

    @Override
    @Transactional
    public void deleteAll(List<UUID> permissions) {
        var delete = permissions.stream()
                .map(this::findById)
                .map(this::deactivatePermission)
                .toList();
        writeRepository.saveAll(delete);
    }

    private Permission deactivatePermission(PermissionDto permissionDto) {
        var permission = new Permission(permissionDto);
        permission.setCode(permissionDto.getCode() + " + " + UUID.randomUUID());
        permission.setStatus(PermissionStatusEnm.INACTIVE);
        return permission;
    }

    @Override
    public PermissionDto findById(UUID id) {
        // Usar findByIdWithModule para cargar el módulo con JOIN FETCH
        return queryRepository.findByIdWithModule(id)
                .map(Permission::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PERMISSION_NOT_FOUND, new ErrorField("id", "Permission not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        filterCriteria(filterCriteria);
        var specifications = new GenericSpecificationsBuilder<Permission>(filterCriteria);
        var data = queryRepository.findAll(specifications, pageable);
        // Inicializar módulos dentro de la transacción para evitar LazyInitializationException
        data.getContent().forEach(permission -> Hibernate.initialize(permission.getModule()));
        return getPaginatedResponse(data);
    }

    private void filterCriteria(List<FilterCriteria> filterCriteria) {
        filterCriteria.forEach(filter -> {
            if ("status".equals(filter.getKey()) && filter.getValue() instanceof String) {
                try {
                    filter.setValue(PermissionStatusEnm.valueOf((String) filter.getValue()));
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid value for enum PermissionStatusEnm: " + filter.getValue());
                }
            }
        });
    }

    private PaginatedResponse getPaginatedResponse(Page<Permission> data) {
        var permissionResponses = data.getContent().stream()
                .map(permission -> new PermissionSearchResponse(permission.toAggregate()))
                .toList();
        return new PaginatedResponse(permissionResponses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    public Long countByCodeAndNotId(String name, UUID id) {
        return queryRepository.countByCodeAndNotId(name, id);
    }
}