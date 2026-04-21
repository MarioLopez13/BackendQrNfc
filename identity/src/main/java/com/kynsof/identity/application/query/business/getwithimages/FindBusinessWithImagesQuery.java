package com.kynsof.identity.application.query.business.getwithimages;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class FindBusinessWithImagesQuery implements IQuery {
    private final UUID businessId;
}
