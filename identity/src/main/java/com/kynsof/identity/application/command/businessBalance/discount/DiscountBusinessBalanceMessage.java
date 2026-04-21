package com.kynsof.identity.application.command.businessBalance.discount;

import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;

@Getter
public class DiscountBusinessBalanceMessage implements ICommandMessage {

    private final boolean result;

    private final String command = "DISCOUNT_BUSINESS_BALANCE";

    public DiscountBusinessBalanceMessage(boolean result) {
        this.result = result;
    }

}
