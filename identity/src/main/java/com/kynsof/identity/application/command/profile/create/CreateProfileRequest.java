package com.kynsof.identity.application.command.profile.create;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProfileRequest {

    private String code;
    private String name;
    private String description;
    private List<UUID> permissionIds;
}
