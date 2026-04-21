package com.kynsof.identity.application.command.user.create;

import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateUserSystemCommand implements ICommand {
    private UUID id;
    private String userName;
    private String email;
    private String name;
    private String lastName;
    private final String password;
    private final EUserType userType;
    private final String image;
    private final String businessId;


    public CreateUserSystemCommand(UUID id, String userName, String email, String name, String lastName, String password,
                                   EUserType userType, String image, String businessId) {
        this.userName = userName;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.userType = userType;
        this.image = image;
        this.id = id;

        this.businessId = businessId;
    }

    public static CreateUserSystemCommand fromRequest(CreateUserSystemRequest request) {
        return new CreateUserSystemCommand(
                UUID.randomUUID(),
                request.getUserName(),
                request.getEmail(),
                request.getName(),
                request.getLastName(), request.getPassword(),
                request.getUserType(), request.getImage(), request.getBusinessId());
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateUserSystemMessage(id);
    }
}
