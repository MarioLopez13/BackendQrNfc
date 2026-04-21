package com.kynsof.identity.application.command.profile.removePermissions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemovePermissionsFromProfileRequest {
    private UUID profileId;
    private List<UUID> permissionIds;
}
