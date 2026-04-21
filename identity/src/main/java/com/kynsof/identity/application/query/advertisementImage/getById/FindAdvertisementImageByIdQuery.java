package com.kynsof.identity.application.query.advertisementImage.getById;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindAdvertisementImageByIdQuery implements IQuery {

    private final UUID id;
}
