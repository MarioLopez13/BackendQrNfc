package com.kynsof.identity.application.command.businessBalance.discount;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DiscountBusinessBalanceCommand implements ICommand {

    private boolean result;
    private final UUID businessId;
    private final Double balance;
    private UUID createdBy;

    public DiscountBusinessBalanceCommand(UUID businessId, Double balance,UUID createdBy) {

        this.businessId = businessId;
        this.balance = balance;
        this.createdBy = createdBy;
    }

    public static DiscountBusinessBalanceCommand fromRequest(DiscountBusinessBalanceRequest request,UUID createdBy) {
        return new DiscountBusinessBalanceCommand(
                request.getBusinessId(),
                request.getBalance(),
                createdBy

        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new DiscountBusinessBalanceMessage(result);
    }
}
