package com.kynsof.identity.application.query.business.getwithimages;

import com.kynsof.identity.application.query.business.geographiclocation.getall.GeographicLocationResponse;
import com.kynsof.identity.application.query.module.getbyid.ModuleResponse;
import com.kynsof.identity.domain.dto.AdvertisementImageDto;
import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class BusinessWithImagesResponse implements IResponse, Serializable {
    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String logo;
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
    private List<AdvertisementImageResponse> advertisementImages;

    public BusinessWithImagesResponse(BusinessDto business, List<AdvertisementImageDto> images) {
        this.id = business.getId();
        this.name = business.getName();
        this.latitude = business.getLatitude();
        this.longitude = business.getLongitude();
        this.description = business.getDescription();
        this.logo = business.getLogo();
        this.ruc = business.getRuc();
        this.status = business.getStatus();
        this.geolocation = business.getGeographicLocationDto() != null
            ? new GeographicLocationResponse(business.getGeographicLocationDto())
            : null;
        this.address = business.getAddress() != null ? business.getAddress() : null;
        this.phone = business.getPhone();
        this.email = business.getEmail();
        this.webSite = business.getWebSite();
        this.storageCapacity = business.getStorageCapacity();
        this.fixedPrice = business.getFixedPrice();
        this.isChargedPerConsultation = business.getIsChargedPerConsultation();
        this.seller = business.getSeller();
        this.zoneId = business.getZoneId();
        this.balance = business.getBalance();
        this.idResponsible = business.getIdResponsible();
        this.modules = business.getModuleDtoList() != null
            ? business.getModuleDtoList().stream()
                .map(moduleDto -> new ModuleResponse(moduleDto.getId(), moduleDto.getName()))
                .toList()
            : new ArrayList<>();
        this.advertisementImages = images != null
            ? images.stream()
                .map(AdvertisementImageResponse::new)
                .collect(Collectors.toList())
            : new ArrayList<>();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdvertisementImageResponse implements Serializable {
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
}
