package com.kynsof.identity.application.query.lead.getbyid;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FindLeadByIdQuery implements IQuery {

    private final UUID id;

    public FindLeadByIdQuery(UUID id) {
        this.id = id;
    }
}
