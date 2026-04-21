package com.kynsof.identity.application.command.profile.update;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class UpdateProfileCommand implements ICommand {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<UUID> permissionIds;

    public UpdateProfileCommand(UUID id, String code, String name, String description, List<UUID> permissionIds) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds;
    }

    public static UpdateProfileCommand fromRequest(UUID id, UpdateProfileRequest request) {
        return new UpdateProfileCommand(
            id,
            request.getCode(),
            request.getName(),
            request.getDescription(),
            request.getPermissionIds()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new UpdateProfileMessage(id);
    }
}
