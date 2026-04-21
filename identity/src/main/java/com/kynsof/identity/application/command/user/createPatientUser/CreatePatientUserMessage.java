package com.kynsof.identity.application.command.user.createPatientUser;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CreatePatientUserMessage implements ICommandMessage {

    private final UUID id;
    private final String command = "CREATE_PATIENT_USER";

    public CreatePatientUserMessage(UUID id) {
        this.id = id;
    }
}
