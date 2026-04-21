package com.kynsof.identity.application.command.businessBalance.create;

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
public class CreateBusinessBalanceCommandHandler implements ICommandHandler<CreateBusinessBalanceCommand> {

    private final IBusinessService service;
    private final IAccessBusiessService accessBusiessService;
    public CreateBusinessBalanceCommandHandler(IBusinessService service, IAccessBusiessService accessBusiessService) {
        this.service = service;
        this.accessBusiessService = accessBusiessService;
    }

    @Override
    @Transactional
    public void handle(CreateBusinessBalanceCommand command) {
        BusinessDto businessDto = this.service.findById(command.getBusinessId());
        businessDto.setBalance(businessDto.getBalance() + command.getBalance());
        service.update(businessDto);
        command.setResult(true);

        AccessBusinessDto accessBusinessDto = new AccessBusinessDto(
                UUID.randomUUID(),
                command.getBusinessId(),
                command.getCreatedBy(),
                EOperationType.INGRESO
        );

        accessBusiessService.create(accessBusinessDto);
    }
}
