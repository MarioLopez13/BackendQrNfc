package com.kynsof.identity.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AdvertisementImageDto implements Serializable {

    private UUID id;
    private String imagenKey;
    private String position;
    private UUID businessId;
    private BusinessDto business;

    public AdvertisementImageDto(UUID id, String imagenKey, String position, UUID businessId) {
        this.id = id;
        this.imagenKey = imagenKey;
        this.position = position;
        this.businessId = businessId;
    }
}
