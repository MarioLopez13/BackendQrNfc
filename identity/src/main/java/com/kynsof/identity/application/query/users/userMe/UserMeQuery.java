package com.kynsof.identity.application.query.users.userMe;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;

@Getter
public class UserMeQuery implements IQuery {

    private final UUID id;
    private final UUID businessId;

    public UserMeQuery(UUID id) {
        this.id = id;
        this.businessId = null;
    }

    public UserMeQuery(UUID id, UUID businessId) {
        this.id = id;
        this.businessId = businessId;
    }
}
