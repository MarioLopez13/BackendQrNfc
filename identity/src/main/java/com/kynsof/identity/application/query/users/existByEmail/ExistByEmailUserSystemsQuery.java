package com.kynsof.identity.application.query.users.existByEmail;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

@Getter
public class ExistByEmailUserSystemsQuery implements IQuery {

    private final String email;

    public ExistByEmailUserSystemsQuery(String email) {
        this.email = email;
    }

}
