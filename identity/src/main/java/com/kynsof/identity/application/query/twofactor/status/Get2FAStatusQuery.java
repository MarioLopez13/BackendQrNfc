package com.kynsof.identity.application.query.twofactor.status;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Get2FAStatusQuery implements IQuery {

    private final UUID userId;

    public Get2FAStatusQuery(UUID userId) {
        this.userId = userId;
    }
}
