package com.kynsof.identity.application.command.advertisementImage.update;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateAdvertisementImageRequest {
    private String imagenKey;
    private String position;
    private UUID businessId;
}
