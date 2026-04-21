package com.kynsof.identity.application.command.user.createPatientUser;

import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreatePatientUserCommand implements ICommand {
    private UUID id;
    private String userName;
    private String email;
    private String name;
    private String lastName;
    private final String password;
    private final String image;

    public CreatePatientUserCommand(String userName, String email, String name, String lastName,
                                    String password, String image) {
        this.userName = userName;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.password = password;
        this.image = image;
    }

    public static CreatePatientUserCommand fromRequest(CreatePatientUserRequest request) {
        return new CreatePatientUserCommand(
                request.getUserName(),
                request.getEmail(),
                request.getName(),
                request.getLastName(),
                request.getPassword(),
                request.getImage()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreatePatientUserMessage(id);
    }
}
