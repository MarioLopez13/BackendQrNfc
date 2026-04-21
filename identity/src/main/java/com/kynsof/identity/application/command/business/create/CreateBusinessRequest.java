package com.kynsof.identity.application.command.business.create;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBusinessRequest {
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private  String image;
    private String ruc;
    private String address;;
    private UUID geographicLocation;
    private String phone;
    private String email;
    private String webSite;
    private String storageCapacity;
    private UUID idResponsible;
    private Double fixedPrice;
    private Boolean isChargedPerConsultation;
    private UUID seller;
    private String zoneId;
}
