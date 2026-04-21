package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.identity.domain.dto.ProfileDto;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IProfileService {

    void create(ProfileDto dto);

    void update(ProfileDto dto);

    void delete(UUID id);

    ProfileDto findById(UUID id);

    ProfileDto findByName(String name);

    ProfileDto findByCode(String code);

    Set<PermissionDto> getPermissionsByProfileName(String profileName);

    Set<PermissionDto> getPermissionsByProfileCode(String profileCode);

    Long countByNameAndNotId(String name, UUID id);

    Long countByCodeAndNotId(String code, UUID id);

    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    void addPermissionsToProfile(UUID profileId, List<UUID> permissionIds);

    void removePermissionsFromProfile(UUID profileId, List<UUID> permissionIds);
}
