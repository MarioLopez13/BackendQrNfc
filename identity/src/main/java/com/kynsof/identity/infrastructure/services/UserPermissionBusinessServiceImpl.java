package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.userPermissionBusiness.getbyid.UserRoleBusinessResponse;
import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.UserPermissionBusinessDto;
import com.kynsof.identity.domain.interfaces.service.IUserPermissionBusinessService;
import com.kynsof.identity.infrastructure.entities.Permission;
import com.kynsof.identity.infrastructure.entities.UserPermissionBusiness;
import com.kynsof.identity.infrastructure.repository.command.UserPermissionBusinessWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.UserPermissionBusinessReadDataJPARepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserPermissionBusinessServiceImpl implements IUserPermissionBusinessService {

    private final UserPermissionBusinessWriteDataJPARepository commandRepository;

    private final UserPermissionBusinessReadDataJPARepository queryRepository;

    public UserPermissionBusinessServiceImpl(UserPermissionBusinessWriteDataJPARepository commandRepository,
                                             UserPermissionBusinessReadDataJPARepository queryRepository) {
        this.commandRepository = commandRepository;
        this.queryRepository = queryRepository;
    }

    @Override
    @Transactional
    public void create(List<UserPermissionBusinessDto> userRoleBusiness) {
        List<UserPermissionBusiness> saveUserRoleBusinesses = new ArrayList<>();
        for (UserPermissionBusinessDto userRoleBusines : userRoleBusiness) {
            //  RulesChecker.checkRule(new UserRoleBusinessMustBeUniqueRule(this, userRoleBusines));

            saveUserRoleBusinesses.add(new UserPermissionBusiness(userRoleBusines));
        }

        this.commandRepository.saveAll(saveUserRoleBusinesses);
    }

    @Override
    public void update(List<UserPermissionBusinessDto> userRoleBusiness) {
        List<UserPermissionBusiness> userRoleBusinesses = userRoleBusiness.stream()
                .map(UserPermissionBusiness::new)
                .collect(Collectors.toList());

        this.commandRepository.saveAll(userRoleBusinesses);
    }

    @Override
    public void delete(UserPermissionBusinessDto delete) {
        this.commandRepository.delete(new UserPermissionBusiness(delete));
    }

    @Transactional
    @Override
    public void delete(UUID businessId, UUID userId) {
        commandRepository.deleteAllByUserIdAndBusinessId(userId, businessId);
    }


    @Override
    @Transactional(readOnly = true)
    public UserPermissionBusinessDto findById(UUID id) {
        // Usar findByIdWithRelations para cargar user, permission, business con JOIN FETCH
        return this.queryRepository.findByIdWithRelations(id)
                .map(UserPermissionBusiness::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.USER_PERMISSION_BUSINESS_NOT_FOUND,
                        new ErrorField("id", "UserPermissionBusiness not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        GenericSpecificationsBuilder<UserPermissionBusiness> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<UserPermissionBusiness> data = this.queryRepository.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }


    private PaginatedResponse getPaginatedResponse(Page<UserPermissionBusiness> data) {
        List<UserRoleBusinessResponse> patients = new ArrayList<>();
        for (UserPermissionBusiness o : data.getContent()) {
            patients.add(new UserRoleBusinessResponse(o.toAggregate()));
        }
        return new PaginatedResponse(patients, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionDto> getPermissionsForUserAndBusiness(UUID userId, UUID businessId) {
        Set<Permission> permissions = this.queryRepository.findPermissionsByUserIdAndBusinessId(userId, businessId);
        List<PermissionDto> permissionDtos = new ArrayList<>();
        for (Permission permission : permissions) {
            permissionDtos.add(permission.toAggregate());
        }
        return permissionDtos;
    }

    @Override
    public Long countByUserAndBusiness(UUID userId, UUID businessId) {
        return this.queryRepository.countByUserAndBusiness(userId, businessId);
    }

    @Override
    public List<Map<String, Object>> countActiveUsersByTypeForBusiness(UUID businessId) {
        // Llamada al repositorio
        List<Object[]> results = this.queryRepository.countActiveUsersByTypeForBusiness(businessId);

        // Convertir resultados a una lista de mapas
        return results.stream()
                .map(result -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("userType", result[0]); // Tipo de usuario
                    entry.put("count", result[1]);   // Conteo de usuarios
                    return entry;
                })
                .collect(Collectors.toList());
    }
}
