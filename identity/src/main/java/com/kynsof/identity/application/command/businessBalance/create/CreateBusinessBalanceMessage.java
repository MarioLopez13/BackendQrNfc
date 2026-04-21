package com.kynsof.identity.application.command.businessBalance.create;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class CreateBusinessBalanceMessage implements ICommandMessage {

    private final boolean result;

    private final String command = "CREATE_BUSINESS_BALANCE";

    public CreateBusinessBalanceMessage(boolean result) {
        this.result = result;
    }

}
