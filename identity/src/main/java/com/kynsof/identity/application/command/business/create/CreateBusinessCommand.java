package com.kynsof.identity.application.command.business.create;

import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateBusinessCommand implements ICommand {

    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String image;
    private String ruc;
    private String address;
    private UUID geographicLocation;
    private final String phone;
    private final String email;
    private String webSite;
    private String storageCapacity;
    private UUID idResponsible;
    private Double fixedPrice;
    private Boolean isChargedPerConsultation;
    private UUID seller;
    private String zoneId;

    public CreateBusinessCommand(String name, String latitude, String longitude, String description, String logo,
                                 String ruc, UUID geographicLocation, String address, String phone, String email,
                                 String webSite, String storageCapacity, UUID idResponsible, Double fixedPrice,
                                 Boolean isChargedPerConsultation,UUID seller,String zoneId) {
        this.phone = phone;
        this.email = email;
        this.id = UUID.randomUUID();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.image = logo;
        this.ruc = ruc;
        this.geographicLocation = geographicLocation;
        this.address = address;
        this.webSite = webSite;
        this.storageCapacity = storageCapacity;
        this.idResponsible = idResponsible;
        this.fixedPrice = fixedPrice;
        this.isChargedPerConsultation = isChargedPerConsultation;
        this.seller = seller;
        this.zoneId = zoneId;
    }

    public static CreateBusinessCommand fromRequest(CreateBusinessRequest request) {
        return new CreateBusinessCommand(
                request.getName(), 
                request.getLatitude(), 
                request.getLongitude(), 
                request.getDescription(), 
                request.getImage(),
                request.getRuc(), 
                request.getGeographicLocation(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getWebSite(),
                request.getStorageCapacity(),
                request.getIdResponsible(),
                request.getFixedPrice(),
                request.getIsChargedPerConsultation(),
                request.getSeller(),
                request.getZoneId()
        );
    }

    @Override
    public ICommandMessage getMessage() {
        return new CreateBusinessMessage(id);
    }
}
