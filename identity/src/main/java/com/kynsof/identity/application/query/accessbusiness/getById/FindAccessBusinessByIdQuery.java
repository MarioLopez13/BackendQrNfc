package com.kynsof.identity.application.query.accessbusiness.getById;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;


@Getter
public class FindAccessBusinessByIdQuery implements IQuery {

    private final UUID id;

    public FindAccessBusinessByIdQuery(UUID id) {
        this.id = id;
    }

}
