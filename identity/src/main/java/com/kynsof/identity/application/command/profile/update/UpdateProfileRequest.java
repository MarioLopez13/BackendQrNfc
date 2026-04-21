package com.kynsof.identity.application.command.profile.update;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateProfileRequest {

    private String code;
    private String name;
    private String description;
    private List<UUID> permissionIds;
}
