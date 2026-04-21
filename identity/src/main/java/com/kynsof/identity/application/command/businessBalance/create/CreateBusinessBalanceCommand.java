package com.kynsof.identity.application.command.businessBalance.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBusinessBalanceCommand implements ICommand {

    private boolean result;
    private final UUID businessId;
    private final Double balance;
    private UUID createdBy;

    public CreateBusinessBalanceCommand(UUID businessId, Double balance,UUID createdBy) {
        this.setResult(Boolean.FALSE);
        this.businessId = businessId;
        this.balance = balance;
        this.createdBy = createdBy;
    }

    public static CreateBusinessBalanceCommand fromRequest(CreateBusinessBalanceRequest request, UUID createdBy) {
        return new CreateBusinessBalanceCommand(
                request.getBusinessId(),
                request.getBalance(),
                createdBy

        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateBusinessBalanceMessage(result);
    }
}
