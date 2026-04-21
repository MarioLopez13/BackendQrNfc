package com.kynsof.identity.application.query.profile.getbyid;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class GetProfileByIdQuery implements IQuery {

    private UUID id;
}
