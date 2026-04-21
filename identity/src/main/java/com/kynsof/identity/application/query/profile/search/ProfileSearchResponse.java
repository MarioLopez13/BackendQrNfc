package com.kynsof.identity.application.query.profile.search;

import com.kynsof.identity.application.query.permission.search.PermissionSearchResponse;
import com.kynsof.identity.domain.dto.ProfileDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProfileSearchResponse implements IResponse {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<PermissionSearchResponse> permissions;
    private LocalDate createdAt;

    public ProfileSearchResponse(ProfileDto dto) {
        this.id = dto.getId();
        this.code = dto.getCode();
        this.name = dto.getName();
        this.description = dto.getDescription();
        this.permissions = dto.getPermissions() != null
            ? dto.getPermissions().stream()
                .map(PermissionSearchResponse::new)
                .collect(Collectors.toList())
            : new ArrayList<>();
        this.createdAt = dto.getCreatedAt() != null ? dto.getCreatedAt().toLocalDate() : null;
    }
}
