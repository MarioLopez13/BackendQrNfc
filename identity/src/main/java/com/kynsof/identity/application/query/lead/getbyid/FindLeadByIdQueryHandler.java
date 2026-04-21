package com.kynsof.identity.application.query.lead.getbyid;

import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.identity.domain.interfaces.service.ILeadService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class FindLeadByIdQueryHandler implements IQueryHandler<FindLeadByIdQuery, LeadDto> {

    private final ILeadService leadService;

    public FindLeadByIdQueryHandler(ILeadService leadService) {
        this.leadService = leadService;
    }

    @Override
    public LeadDto handle(FindLeadByIdQuery query) {
        return leadService.findById(query.getId());
    }
}
