package com.kynsof.identity.domain.dto;

import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BusinessDto  implements Serializable {
    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String logo;
    private String ruc;
    private String address;
    private EBusinessStatus status;
    private boolean deleted;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime deleteAt;
    private double balance;
    private String phone;
    private String email;
    private String webSite;
    private String storageCapacity;
    private UUID idResponsible;
    private Double fixedPrice;
    private Boolean isChargedPerConsultation;
    private UUID seller;
    private String zoneId;

    private GeographicLocationDto geographicLocationDto;
    private List<ModuleDto> moduleDtoList;

    public BusinessDto(UUID id, String name, String latitude, String longitude, String description, String logo,
                       String ruc, EBusinessStatus status, GeographicLocationDto geographicLocationDto, String address,
                       String phone, String email, String webSite, String storageCapacity,UUID idResponsible,
                       Double fixedPrice,Boolean isChargedPerConsultation,UUID seller,String zoneId) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.logo = logo;
        this.ruc = ruc;
        this.status = status;
        this.geographicLocationDto = geographicLocationDto;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.webSite = webSite;
        this.storageCapacity = storageCapacity;
        this.idResponsible = idResponsible;
        this.fixedPrice = fixedPrice;
        this.isChargedPerConsultation = isChargedPerConsultation;
        this.seller = seller;
        this.zoneId = zoneId;
    }

}
