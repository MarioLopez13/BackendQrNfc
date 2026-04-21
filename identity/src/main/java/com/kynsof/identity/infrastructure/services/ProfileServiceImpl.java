package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.profile.search.ProfileSearchResponse;
import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.ProfileDto;
import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.identity.infrastructure.entities.Permission;
import com.kynsof.identity.infrastructure.entities.Profile;
import com.kynsof.identity.infrastructure.repository.command.ProfileWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.PermissionReadDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.ProfileReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileServiceImpl implements IProfileService {

    private final ProfileWriteDataJPARepository writeRepository;
    private final ProfileReadDataJPARepository queryRepository;
    private final PermissionReadDataJPARepository permissionRepository;

    @Autowired
    public ProfileServiceImpl(ProfileWriteDataJPARepository writeRepository,
                              ProfileReadDataJPARepository queryRepository,
                              PermissionReadDataJPARepository permissionRepository) {
        this.writeRepository = writeRepository;
        this.queryRepository = queryRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void create(ProfileDto dto) {
        Profile profile = new Profile(dto);
        if (dto.getPermissions() != null && !dto.getPermissions().isEmpty()) {
            Set<Permission> permissions = dto.getPermissions().stream()
                    .map(p -> permissionRepository.findById(p.getId()).orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toSet());
            profile.setPermissions(permissions);
        }
        writeRepository.save(profile);
    }

    @Override
    @Transactional
    public void update(ProfileDto dto) {
        Profile profile = queryRepository.findByIdWithPermissions(dto.getId())
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("id", "Profile not found."))));

        profile.setCode(dto.getCode());
        profile.setName(dto.getName());
        profile.setDescription(dto.getDescription());
        profile.setUpdatedAt(LocalDateTime.now());

        if (dto.getPermissions() != null) {
            Set<Permission> permissions = dto.getPermissions().stream()
                    .map(p -> permissionRepository.findById(p.getId()).orElse(null))
                    .filter(p -> p != null)
                    .collect(Collectors.toSet());
            profile.setPermissions(permissions);
        }

        writeRepository.save(profile);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Profile profile = queryRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("id", "Profile not found."))));

        profile.getPermissions().clear();
        writeRepository.save(profile);
        writeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto findById(UUID id) {
        return queryRepository.findByIdWithPermissions(id)
                .map(Profile::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("id", "Profile not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto findByName(String name) {
        return queryRepository.findByNameWithPermissions(name)
                .map(Profile::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("name", "Profile not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDto findByCode(String code) {
        return queryRepository.findByCodeWithPermissions(code)
                .map(Profile::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("code", "Profile not found."))));
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionDto> getPermissionsByProfileName(String profileName) {
        return queryRepository.findByNameWithPermissions(profileName)
                .map(profile -> profile.getPermissions().stream()
                        .map(Permission::toAggregate)
                        .collect(Collectors.toSet()))
                .orElse(new HashSet<>());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissionDto> getPermissionsByProfileCode(String profileCode) {
        return queryRepository.findByCodeWithPermissions(profileCode)
                .map(profile -> profile.getPermissions().stream()
                        .map(Permission::toAggregate)
                        .collect(Collectors.toSet()))
                .orElse(new HashSet<>());
    }

    @Override
    public Long countByNameAndNotId(String name, UUID id) {
        return queryRepository.countByNameAndNotId(name, id);
    }

    @Override
    public Long countByCodeAndNotId(String code, UUID id) {
        return queryRepository.countByCodeAndNotId(code, id);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        var specifications = new GenericSpecificationsBuilder<Profile>(filterCriteria);
        var data = queryRepository.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<Profile> data) {
        var profileResponses = data.getContent().stream()
                .map(profile -> new ProfileSearchResponse(profile.toAggregate()))
                .toList();
        return new PaginatedResponse(profileResponses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    @Transactional
    public void addPermissionsToProfile(UUID profileId, List<UUID> permissionIds) {
        Profile profile = queryRepository.findByIdWithPermissions(profileId)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("id", "Profile not found."))));

        Set<Permission> newPermissions = permissionIds.stream()
                .map(id -> permissionRepository.findById(id).orElse(null))
                .filter(p -> p != null)
                .collect(Collectors.toSet());

        profile.getPermissions().addAll(newPermissions);
        writeRepository.save(profile);
    }

    @Override
    @Transactional
    public void removePermissionsFromProfile(UUID profileId, List<UUID> permissionIds) {
        Profile profile = queryRepository.findByIdWithPermissions(profileId)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.PROFILE_NOT_FOUND, new ErrorField("id", "Profile not found."))));

        profile.getPermissions().removeIf(p -> permissionIds.contains(p.getId()));
        writeRepository.save(profile);
    }
}
