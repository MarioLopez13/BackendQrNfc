package com.kynsof.identity.application.query.business.getbyid;

import com.kynsof.identity.application.query.business.geographiclocation.getall.GeographicLocationResponse;
import com.kynsof.identity.application.query.module.getbyid.ModuleResponse;
import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class BusinessByIdResponse implements IResponse, Serializable {
    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String image;
    private String ruc;
    private String address;
    private EBusinessStatus status;
    private String phone;
    private String email;
    private String webSite;
    private String storageCapacity;
    private Double fixedPrice;
    private Boolean isChargedPerConsultation;
    private UUID seller;
    private String zoneId;
    private UUID idResponsible;
    private double balance;

    private GeographicLocationResponse geolocation;
    private List<ModuleResponse> modules;

    public BusinessByIdResponse(BusinessDto object) {
        this.id = object.getId();
        this.name = object.getName();
        this.latitude = object.getLatitude();
        this.longitude = object.getLongitude();
        this.description = object.getDescription();
        this.image = object.getLogo();
        this.ruc = object.getRuc();
        this.status = object.getStatus();
        this.geolocation = object.getGeographicLocationDto() != null ? new GeographicLocationResponse(object.getGeographicLocationDto()) : null;
        this.address = object.getAddress() != null ? object.getAddress() : null;
        this.phone = object.getPhone();
        this.email = object.getEmail();
        this.webSite = object.getWebSite();
        this.storageCapacity = object.getStorageCapacity();
        this.fixedPrice = object.getFixedPrice();
        this.isChargedPerConsultation = object.getIsChargedPerConsultation();
        this.seller = object.getSeller();
        this.zoneId = object.getZoneId();
        this.balance = object.getBalance();
        this.idResponsible = object.getIdResponsible();
        modules = object.getModuleDtoList() !=null ? object.getModuleDtoList().stream()
                .map(moduleDto ->
                        new ModuleResponse(moduleDto.getId(), moduleDto.getName()))
                .toList(): new ArrayList<>();
    }

    public BusinessByIdResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

}