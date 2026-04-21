package com.kynsof.identity.application.command.business.update;

import com.kynsof.identity.domain.dto.enumType.EBusinessStatus;
import com.kynsof.share.core.domain.bus.command.ICommand;
import com.kynsof.share.core.domain.bus.command.ICommandMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UpdateBusinessCommand implements ICommand {

    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String description;
    private String logo;
    private String ruc;
    private EBusinessStatus status;
    private UUID geographicLocation;
    private String address;
    private final String phone;
    private final String email;
    private String webSite;
    private String storageCapacity;
    private UUID idResponsible;
    private Double fixedPrice;
    private Boolean isChargedPerConsultation;
    private UUID seller;
    private String zoneId;

    public UpdateBusinessCommand(UUID id, String name, String latitude, String longitude, String description,
                                 String logo, String ruc, EBusinessStatus status, UUID geographicLocation,
                                 String address, String phone, String email, String webSite, String storageCapacity,
                                 UUID idResponsible, Double fixedPrice,
                                 Boolean isChargedPerConsultation,UUID seller,String zoneId) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.logo = logo;
        this.ruc = ruc;
        this.status = status;
        this.geographicLocation = geographicLocation;
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

    public static UpdateBusinessCommand fromRequest(UpdateBusinessRequest request, UUID id) {
        return new UpdateBusinessCommand(
                id,
                request.getName(),
                request.getLatitude(),
                request.getLongitude(),
                request.getDescription(),
                request.getImage(),
                request.getRuc(),
                request.getStatus(),
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
        return new UpdateBusinessMessage(id);
    }
}
