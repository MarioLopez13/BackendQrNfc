package com.kynsof.identity.application.command.lead.capture;

import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.identity.domain.interfaces.service.ILeadService;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.exception.BusinessException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import org.springframework.stereotype.Component;

@Component
public class CaptureLeadCommandHandler implements ICommandHandler<CaptureLeadCommand> {

    private final ILeadService leadService;

    public CaptureLeadCommandHandler(ILeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public void handle(CaptureLeadCommand command) {
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getEmail(), "email", "El email es requerido"));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getContactName(), "contactName", "El nombre de contacto es requerido"));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getType(), "type", "El tipo de lead es requerido"));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getSource(), "source", "La fuente del lead es requerida"));

        if (leadService.existsByEmail(command.getEmail())) {
            throw new BusinessException(DomainErrorMessage.LEAD_EMAIL_ALREADY_EXISTS,
                    "Ya existe un lead con este email: " + command.getEmail());
        }

        LeadDto dto = new LeadDto();
        dto.setId(command.getId());
        dto.setType(command.getType());
        dto.setBusinessName(command.getBusinessName());
        dto.setContactName(command.getContactName());
        dto.setEmail(command.getEmail());
        dto.setPhone(command.getPhone());
        dto.setCity(command.getCity());
        dto.setSpecialty(command.getSpecialty());
        dto.setEmployeeCount(command.getEmployeeCount());
        dto.setMessage(command.getLeadMessage());
        dto.setSource(command.getSource());
        dto.setStatus(ELeadStatus.NEW);

        leadService.create(dto);
    }
}
