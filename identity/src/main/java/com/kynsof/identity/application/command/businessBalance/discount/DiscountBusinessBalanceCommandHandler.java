package com.kynsof.identity.application.command.businessBalance.discount;

import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.enumType.EOperationType;
import com.kynsof.identity.domain.interfaces.service.IAccessBusiessService;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class DiscountBusinessBalanceCommandHandler implements ICommandHandler<DiscountBusinessBalanceCommand> {

    private final IBusinessService service;
    private final IAccessBusiessService accessBusiessService;
    public DiscountBusinessBalanceCommandHandler(IBusinessService service, IAccessBusiessService accessBusiessService) {
        this.service = service;
        this.accessBusiessService = accessBusiessService;
    }

    @Override
    @Transactional
    public void handle(DiscountBusinessBalanceCommand command) {
        BusinessDto businessDto = this.service.findById(command.getBusinessId());
        if (businessDto.getBalance() <= 0) {
            throw new IllegalArgumentException("Cannot apply discount: Business balance is zero or negative.");
        }
        businessDto.setBalance(businessDto.getBalance() - command.getBalance());
        service.update(businessDto);

        AccessBusinessDto accessBusinessDto = new AccessBusinessDto(
                UUID.randomUUID(),
                command.getBusinessId(),
                command.getCreatedBy(),
                EOperationType.EGRESO
        );

        accessBusiessService.create(accessBusinessDto);
    }
}
