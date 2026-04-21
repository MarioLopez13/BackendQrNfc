package com.kynsof.identity.application.query.business.getbyid.httpReplicate;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;

@Getter
public class FindBusinessByIdReplicateHttpQuery  implements IQuery {

    private final UUID id;

    public FindBusinessByIdReplicateHttpQuery(UUID id) {
        this.id = id;
    }

}
