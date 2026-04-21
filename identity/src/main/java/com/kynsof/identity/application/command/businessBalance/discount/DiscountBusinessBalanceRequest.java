package com.kynsof.identity.application.command.businessBalance.discount;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class DiscountBusinessBalanceRequest {
    private UUID businessId;
    private Double balance;

}
