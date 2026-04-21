package com.kynsof.identity.application.command.profile.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CreateProfileCommand implements ICommand {

    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<UUID> permissionIds;

    public CreateProfileCommand(String code, String name, String description, List<UUID> permissionIds) {
        this.id = UUID.randomUUID();
        this.code = code;
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds;
    }

    public static CreateProfileCommand fromRequest(CreateProfileRequest request) {
        return new CreateProfileCommand(
            request.getCode(),
            request.getName(),
            request.getDescription(),
            request.getPermissionIds()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateProfileMessage(id);
    }
}
