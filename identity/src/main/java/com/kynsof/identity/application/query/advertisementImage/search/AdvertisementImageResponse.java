package com.kynsof.identity.application.query.advertisementImage.search;

import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AdvertisementImageResponse implements IResponse , Serializable {

    private UUID id;
    private String imagenKey;
    private String position;
    private UUID businessId;

    public AdvertisementImageResponse(AdvertisementImageDto dto) {
        this.id = dto.getId();
        this.imagenKey = dto.getImagenKey();
        this.position = dto.getPosition();
        this.businessId = dto.getBusinessId();
    }
}
