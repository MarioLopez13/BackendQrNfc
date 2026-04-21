package com.kynsof.identity.application.query.dashboard.GetCountUserTypeByBusinessId;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;

@Getter
public class GetCountUserTypeByBusinessIdQuery implements IQuery {

    private final UUID businessId;

    public GetCountUserTypeByBusinessIdQuery(UUID id) {
        this.businessId = id;
    }

}
