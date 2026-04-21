package com.kynsof.identity.application.command.business.update;

import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateBusinessRequest {
    private String name;    
    private String latitude;
    private String longitude;
    private String description;
    private String image;
    private String ruc;
    private EBusinessStatus status;
    private UUID geographicLocation;
    private String address;
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