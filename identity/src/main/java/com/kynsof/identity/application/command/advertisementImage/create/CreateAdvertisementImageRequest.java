package com.kynsof.identity.application.command.advertisementImage.create;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateAdvertisementImageRequest {
    private String imagenKey;
    private String position;
    private UUID businessId;
}
