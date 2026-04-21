package com.kynsof.identity.application.query.users.userByBusiness;
import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Getter
public class FindUsersByBusinessQuery implements IQuery {
    private final UUID businessId;
    private final String email;
    private final String name;
    private final String lastName;
    private final Pageable pageable;

    public FindUsersByBusinessQuery(UUID businessId, String email, String name, String lastName, Pageable pageable) {
        this.businessId = businessId;
        this.email = email;
        this.name = name;
        this.lastName = lastName;
        this.pageable = pageable;
    }
}