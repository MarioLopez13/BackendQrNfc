package com.kynsof.identity.application.command.businessBalance.create;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBusinessBalanceRequest {
    private UUID businessId;
    private Double balance;

}
